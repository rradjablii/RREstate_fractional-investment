package com.rrestate.distribution;

import com.rrestate.audit.AuditService;
import com.rrestate.common.ApiException;
import com.rrestate.common.MoneyRules;
import com.rrestate.common.PageResponse;
import com.rrestate.common.Paging;
import com.rrestate.portfolio.Position;
import com.rrestate.portfolio.PositionRepository;
import com.rrestate.property.Property;
import com.rrestate.property.PropertyRepository;
import com.rrestate.transaction.TransactionService;
import com.rrestate.transaction.TransactionType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pro-rata distribution accounting. Each allocation = total * position / sum(positions), rounded DOWN
 * to 4 dp, so allocations never exceed the total; any sub-cent remainder stays undistributed.
 */
@Service
public class DistributionService {
    public record CreateDistributionRequest(@NotNull UUID propertyId, @NotNull @Positive @Digits(integer = 15, fraction = 2) BigDecimal totalAmount,
                                            @NotNull LocalDate periodStart, @NotNull LocalDate periodEnd) {}
    public record DistributionResponse(UUID id, String propertyName, String amount, String currency, String status, LocalDate periodStart, LocalDate periodEnd, Instant paidAt) {}
    public record AdminDistributionResponse(UUID id, UUID propertyId, String totalAmount, String currency, DistributionStatus status, int allocations, LocalDate periodStart, LocalDate periodEnd) {}

    private final DistributionRepository distributions;
    private final AllocationRepository allocations;
    private final PositionRepository positions;
    private final PropertyRepository properties;
    private final TransactionService ledger;
    private final AuditService audit;
    private final Paging paging;
    private final Clock clock;

    public DistributionService(DistributionRepository distributions, AllocationRepository allocations, PositionRepository positions,
                               PropertyRepository properties, TransactionService ledger, AuditService audit, Paging paging, Clock clock) {
        this.distributions = distributions; this.allocations = allocations; this.positions = positions; this.properties = properties;
        this.ledger = ledger; this.audit = audit; this.paging = paging; this.clock = clock;
    }

    @Transactional
    public AdminDistributionResponse create(UUID actor, CreateDistributionRequest req) {
        if (req.periodEnd().isBefore(req.periodStart())) throw ApiException.unprocessable("INVALID_PERIOD", "The period end must not be before its start.");
        Property property = properties.findById(req.propertyId()).orElseThrow(() -> ApiException.notFound("Property"));
        List<Position> eligible = positions.findByPropertyId(property.getId());
        BigDecimal whole = eligible.stream().map(Position::getInvestedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (eligible.isEmpty() || whole.signum() == 0) throw ApiException.unprocessable("NO_INVESTORS", "This property has no confirmed investors.");
        Distribution d = distributions.saveAndFlush(new Distribution(property.getId(), req.totalAmount(), property.getCurrency(), req.periodStart(), req.periodEnd(), actor, Instant.now(clock)));
        for (Position p : eligible) {
            allocations.save(new Allocation(d.getId(), p.getUserId(), p.getId(), MoneyRules.share(req.totalAmount(), p.getInvestedAmount(), whole), property.getCurrency()));
        }
        audit.record(actor, "DISTRIBUTION_CREATED", "Distribution", d.getId(), MoneyRules.str(req.totalAmount()) + " " + property.getCurrency());
        return admin(d, eligible.size());
    }

    @Transactional
    public void approve(UUID actor, UUID id) {
        Distribution d = distributions.findById(id).orElseThrow(() -> ApiException.notFound("Distribution"));
        if (d.getStatus() != DistributionStatus.CREATED) throw ApiException.conflict("INVALID_STATE", "Only newly created distributions can be approved.");
        d.setStatus(DistributionStatus.APPROVED);
        d.setApprovedAt(Instant.now(clock));
        audit.record(actor, "DISTRIBUTION_APPROVED", "Distribution", id, null);
    }

    /** Records that payment of an approved distribution was confirmed; writes one ledger entry per investor. */
    @Transactional
    public void markPaid(UUID actor, UUID id) {
        Distribution d = distributions.findById(id).orElseThrow(() -> ApiException.notFound("Distribution"));
        if (d.getStatus() != DistributionStatus.APPROVED) throw ApiException.conflict("INVALID_STATE", "Only approved distributions can be marked paid.");
        Instant now = Instant.now(clock);
        for (Allocation a : allocations.findByDistributionId(id)) {
            a.setStatus("PAID");
            a.setPaidAt(now);
            ledger.record(a.getUserId(), TransactionType.DISTRIBUTION_PAID, "COMPLETED", a.getAmount(), a.getCurrency(), d.getPropertyId(), null, id.toString());
        }
        d.setStatus(DistributionStatus.PAID);
        audit.record(actor, "DISTRIBUTION_PAID", "Distribution", id, null);
    }

    @Transactional(readOnly = true)
    public PageResponse<DistributionResponse> listForUser(UUID userId, int page, int size) {
        var result = allocations.findByUserId(userId, paging.of(page, size, Sort.by(Sort.Direction.DESC, "id")));
        Map<UUID, Distribution> ds = distributions.findAllById(result.stream().map(Allocation::getDistributionId).collect(Collectors.toSet())).stream().collect(Collectors.toMap(Distribution::getId, x -> x));
        Map<UUID, String> names = properties.findAllById(ds.values().stream().map(Distribution::getPropertyId).collect(Collectors.toSet())).stream().collect(Collectors.toMap(Property::getId, Property::getName));
        return PageResponse.of(result, a -> {
            Distribution d = ds.get(a.getDistributionId());
            return new DistributionResponse(a.getId(), names.get(d.getPropertyId()), MoneyRules.str(a.getAmount()), a.getCurrency(), a.getStatus(), d.getPeriodStart(), d.getPeriodEnd(), a.getPaidAt());
        });
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminDistributionResponse> listAll(int page, int size) {
        return PageResponse.of(distributions.findAll(paging.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))), d -> admin(d, allocations.findByDistributionId(d.getId()).size()));
    }

    private static AdminDistributionResponse admin(Distribution d, int n) {
        return new AdminDistributionResponse(d.getId(), d.getPropertyId(), MoneyRules.str(d.getTotalAmount()), d.getCurrency(), d.getStatus(), n, d.getPeriodStart(), d.getPeriodEnd());
    }
}
