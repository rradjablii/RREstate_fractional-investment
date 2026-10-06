package com.rrestate.transaction;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.UuidGenerator;

/** Append-only ledger entry. Never updated after insert. */
@Entity
@Table(name = "transactions")
@Immutable
@Getter
public class Transaction {
    @Id @GeneratedValue @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;
    private UUID userId;
    @Enumerated(EnumType.STRING)
    private TransactionType type;
    private String status;
    private BigDecimal amount;
    private String currency;
    private UUID propertyId;
    private UUID investmentId;
    private String reference;
    private Instant createdAt;

    protected Transaction() {}

    public Transaction(UUID userId, TransactionType type, String status, BigDecimal amount, String currency,
                       UUID propertyId, UUID investmentId, String reference, Instant createdAt) {
        this.userId = userId; this.type = type; this.status = status; this.amount = amount; this.currency = currency;
        this.propertyId = propertyId; this.investmentId = investmentId; this.reference = reference; this.createdAt = createdAt;
    }
}
