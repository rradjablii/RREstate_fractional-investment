package com.rrestate.transaction;

import com.rrestate.common.PageResponse;
import com.rrestate.common.Paging;
import com.rrestate.property.Property;
import com.rrestate.property.PropertyRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {
    public record TransactionResponse(UUID id, String type, String status, String amount, String currency, String propertyName, String reference, Instant createdAt) {}

    private final TransactionRepository repo;
    private final PropertyRepository properties;
    private final Paging paging;
    private final Clock clock;

    public TransactionService(TransactionRepository repo, PropertyRepository properties, Paging paging, Clock clock) {
        this.repo = repo; this.properties = properties; this.paging = paging; this.clock = clock;
    }

    /** Must run inside the caller's transaction so the ledger entry commits with the state change. */
    public void record(UUID userId, TransactionType type, String status, BigDecimal amount, String currency, UUID propertyId, UUID investmentId, String reference) {
        repo.save(new Transaction(userId, type, status, amount, currency, propertyId, investmentId, reference, Instant.now(clock)));
    }

    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> listForUser(UUID userId, int page, int size) {
        var result = repo.findByUserId(userId, paging.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        Map<UUID, String> names = properties.findAllById(result.stream().map(Transaction::getPropertyId).filter(java.util.Objects::nonNull).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(Property::getId, Property::getName));
        return PageResponse.of(result, t -> new TransactionResponse(t.getId(), t.getType().name(), t.getStatus(), com.rrestate.common.MoneyRules.str(t.getAmount()),
                t.getCurrency(), t.getPropertyId() == null ? null : names.get(t.getPropertyId()), t.getReference(), t.getCreatedAt()));
    }
}
