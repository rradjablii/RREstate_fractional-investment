package com.rrestate.investment;

import com.rrestate.audit.AuditService;
import com.rrestate.auth.KycStatus;
import com.rrestate.auth.User;
import com.rrestate.auth.UserRepository;
import com.rrestate.common.ApiException;
import com.rrestate.common.MoneyRules;
import com.rrestate.common.PageResponse;
import com.rrestate.common.Paging;
import com.rrestate.config.AppProperties;
import com.rrestate.investment.InvestmentDtos.*;
import com.rrestate.portfolio.Position;
import com.rrestate.portfolio.PositionRepository;
import com.rrestate.property.Opportunity;
import com.rrestate.property.OpportunityRepository;
import com.rrestate.property.Property;
import com.rrestate.property.PropertyRepository;
import com.rrestate.property.PropertyStatus;
import com.rrestate.transaction.TransactionService;
import com.rrestate.transaction.TransactionType;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Investment lifecycle: create (idempotent, capacity-reserving) -> payment confirmed (server side) -> position.
 * No payment provider is integrated yet: confirmation/failure are recorded by an operator via the admin API,
 * never by the browser.
 */
@Service
public class InvestmentService {
    private final InvestmentRepository investments;
    private final OpportunityRepository opportunities;
    private final PropertyRepository properties;
    private final PositionRepository positions;
    private final UserRepository users;
    private final TransactionService ledger;
    private final AuditService audit;
    private final AppProperties props;
    private final Paging paging;
    private final Clock clock;
    private final TransactionTemplate tx;

    public InvestmentService(InvestmentRepository investments, OpportunityRepository opportunities, PropertyRepository properties,
                             PositionRepository positions, UserRepository users, TransactionService ledger, AuditService audit,
                             AppProperties props, Paging paging, Clock clock, PlatformTransactionManager tm) {
        this.investments = investments; this.opportunities = opportunities; this.properties = properties; this.positions = positions;
        this.users = users; this.ledger = ledger; this.audit = audit; this.props = props; this.paging = paging; this.clock = clock;
        this.tx = new TransactionTemplate(tm);
    }

    /** Idempotent on (user, Idempotency-Key): a replay returns the original investment; a mismatched replay is a 409. */
    public InvestmentResponse create(UUID userId, CreateInvestmentRequest req, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 100) {
            throw new ApiException(org.springframework.http.HttpStatus.BAD_REQUEST, "BAD_REQUEST", "A valid Idempotency-Key header is required.");
        }
        if (!props.compliance().investmentEnabled()) {
            throw ApiException.unprocessable("INVESTMENT_DISABLED", "Investing is not available yet.");
        }
        try {
            Investment inv = tx.execute(s -> createInTx(userId, req, idempotencyKey));
            return toResponse(inv, propertyName(inv.getPropertyId()));
        } catch (DataIntegrityViolationException race) {
            // Concurrent request with the same key won the unique constraint: return its result.
            Investment existing = investments.findByUserIdAndIdempotencyKey(userId, idempotencyKey).orElseThrow(() -> race);
            assertSameRequest(existing, req);
            return toResponse(existing, propertyName(existing.getPropertyId()));
        }
    }

    private Investment createInTx(UUID userId, CreateInvestmentRequest req, String key) {
        var existing = investments.findByUserIdAndIdempotencyKey(userId, key);
        if (existing.isPresent()) {
            assertSameRequest(existing.get(), req);
            return existing.get();
        }
        User user = users.findById(userId).orElseThrow(() -> ApiException.unauthorized("UNAUTHENTICATED", "Account not found."));
        if (props.compliance().kycRequired() && user.getKycStatus() != KycStatus.VERIFIED) {
            throw ApiException.unprocessable("KYC_REQUIRED", "Identity verification is required before investing.");
        }
        Opportunity opp = opportunities.findById(req.opportunityId()).orElseThrow(() -> ApiException.notFound("Investment opportunity"));
        Property property = properties.findById(opp.getPropertyId()).orElseThrow(() -> ApiException.notFound("Property"));
        if (property.getStatus() != PropertyStatus.OPEN || !opp.isInvestmentEnabled()) {
            throw ApiException.unprocessable("OPPORTUNITY_CLOSED", "This property is not open for investment.");
        }
        if (!property.getCurrency().equals(req.currency())) {
            throw ApiException.unprocessable("CURRENCY_MISMATCH", "This property accepts investments in " + property.getCurrency() + ".");
        }
        if (req.amount().compareTo(opp.getMinimumInvestment()) < 0) {
            throw ApiException.unprocessable("BELOW_MINIMUM", "The minimum investment is " + MoneyRules.str(opp.getMinimumInvestment()) + " " + property.getCurrency() + ".");
        }
        // Atomic capacity reservation in SQL: two concurrent requests cannot both exceed the target.
        if (opportunities.reserve(opp.getId(), req.amount()) == 0) {
            throw ApiException.unprocessable("CAPACITY_EXCEEDED", "There is not enough remaining capacity for this amount.");
        }
        Opportunity fresh = opportunities.findById(opp.getId()).orElseThrow();
        if (fresh.getRaisedAmount().compareTo(fresh.getTargetRaise()) >= 0) {
            // reserve() clears the persistence context, so reload before mutating.
            properties.findById(property.getId()).ifPresent(p -> p.setStatus(PropertyStatus.FUNDED));
        }
        BigDecimal interest = MoneyRules.percentOf(req.amount(), opp.getTargetRaise(), MoneyRules.INTEREST_SCALE);
        Investment inv = investments.saveAndFlush(new Investment(userId, opp.getId(), property.getId(), req.amount(), req.currency(),
                InvestmentStatus.PAYMENT_PENDING, interest, key, Instant.now(clock)));
        ledger.record(userId, TransactionType.INVESTMENT_CREATED, "PENDING", inv.getAmount(), inv.getCurrency(), property.getId(), inv.getId(), key);
        audit.record(userId, "INVESTMENT_CREATED", "Investment", inv.getId(), MoneyRules.str(inv.getAmount()) + " " + inv.getCurrency());
        return inv;
    }

    private void assertSameRequest(Investment existing, CreateInvestmentRequest req) {
        boolean same = existing.getOpportunityId().equals(req.opportunityId()) && existing.getAmount().compareTo(req.amount()) == 0
                && existing.getCurrency().equals(req.currency());
        if (!same) throw ApiException.conflict("IDEMPOTENCY_KEY_REUSED", "This Idempotency-Key was already used for a different request.");
    }

    /** Operator records a verified payment (stand-in for a signed provider webhook). */
    @Transactional
    public void confirmPayment(UUID actor, UUID investmentId) {
        Investment inv = investments.lockById(investmentId).orElseThrow(() -> ApiException.notFound("Investment"));
        if (inv.getStatus() != InvestmentStatus.PAYMENT_PENDING) {
            throw ApiException.conflict("INVALID_STATE", "Only investments awaiting payment can be confirmed.");
        }
        Instant now = Instant.now(clock);
        inv.setStatus(InvestmentStatus.CONFIRMED);
        inv.setInvestmentDate(now);
        inv.setUpdatedAt(now);
        Position pos = positions.lockByUserAndProperty(inv.getUserId(), inv.getPropertyId()).orElseGet(() -> positions.saveAndFlush(new Position(inv.getUserId(), inv.getPropertyId(), inv.getCurrency(), now)));
        pos.add(inv.getAmount(), inv.getInterestPercent(), now);
        ledger.record(inv.getUserId(), TransactionType.PAYMENT_COMPLETED, "COMPLETED", inv.getAmount(), inv.getCurrency(), inv.getPropertyId(), inv.getId(), null);
        ledger.record(inv.getUserId(), TransactionType.INVESTMENT_CONFIRMED, "COMPLETED", inv.getAmount(), inv.getCurrency(), inv.getPropertyId(), inv.getId(), null);
        audit.record(actor, "INVESTMENT_CONFIRMED", "Investment", inv.getId(), null);
    }

    /** Operator records a failed payment; reserved capacity is released. */
    @Transactional
    public void failPayment(UUID actor, UUID investmentId) {
        Investment inv = investments.lockById(investmentId).orElseThrow(() -> ApiException.notFound("Investment"));
        if (inv.getStatus() != InvestmentStatus.PAYMENT_PENDING) {
            throw ApiException.conflict("INVALID_STATE", "Only investments awaiting payment can be failed.");
        }
        inv.setStatus(InvestmentStatus.FAILED);
        inv.setUpdatedAt(Instant.now(clock));
        opportunities.release(inv.getOpportunityId(), inv.getAmount());
        properties.findById(inv.getPropertyId()).filter(p -> p.getStatus() == PropertyStatus.FUNDED).ifPresent(p -> p.setStatus(PropertyStatus.OPEN));
        ledger.record(inv.getUserId(), TransactionType.PAYMENT_FAILED, "FAILED", inv.getAmount(), inv.getCurrency(), inv.getPropertyId(), inv.getId(), null);
        audit.record(actor, "INVESTMENT_PAYMENT_FAILED", "Investment", inv.getId(), null);
    }

    @Transactional(readOnly = true)
    public PageResponse<InvestmentResponse> listForUser(UUID userId, int page, int size) {
        var result = investments.findByUserId(userId, paging.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        Map<UUID, String> names = names(result.getContent().stream().map(Investment::getPropertyId).toList());
        return PageResponse.of(result, i -> toResponse(i, names.get(i.getPropertyId())));
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminInvestmentResponse> listAll(int page, int size) {
        var result = investments.findAll(paging.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        Map<UUID, String> names = names(result.getContent().stream().map(Investment::getPropertyId).toList());
        Map<UUID, String> emails = users.findAllById(result.getContent().stream().map(Investment::getUserId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(User::getId, User::getEmail));
        return PageResponse.of(result, i -> new AdminInvestmentResponse(i.getId(), i.getPropertyId(), names.get(i.getPropertyId()), MoneyRules.str(i.getAmount()),
                i.getCurrency(), i.getStatus(), MoneyRules.str(i.getInterestPercent()), i.getInvestmentDate(), i.getCreatedAt(), emails.get(i.getUserId())));
    }

    private Map<UUID, String> names(Collection<UUID> propertyIds) {
        return properties.findAllById(propertyIds.stream().collect(Collectors.toSet())).stream().collect(Collectors.toMap(Property::getId, Property::getName));
    }

    private String propertyName(UUID id) { return properties.findById(id).map(Property::getName).orElse(null); }

    private static InvestmentResponse toResponse(Investment i, String propertyName) {
        return new InvestmentResponse(i.getId(), i.getPropertyId(), propertyName, MoneyRules.str(i.getAmount()), i.getCurrency(), i.getStatus(),
                MoneyRules.str(i.getInterestPercent()), i.getInvestmentDate(), i.getCreatedAt());
    }
}
