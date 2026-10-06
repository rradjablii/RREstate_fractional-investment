package com.rrestate.investment;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "investments")
@Getter
public class Investment {
    @Id @GeneratedValue @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;
    private UUID userId;
    private UUID opportunityId;
    private UUID propertyId;
    private BigDecimal amount;
    private String currency;
    @Enumerated(EnumType.STRING) @Setter
    private InvestmentStatus status;
    /** Economic interest fixed at creation: amount / target raise. Not recomputed from current property data. */
    private BigDecimal interestPercent;
    private String idempotencyKey;
    @Setter private Instant investmentDate;
    private Instant createdAt;
    @Setter private Instant updatedAt;

    protected Investment() {}

    public Investment(UUID userId, UUID opportunityId, UUID propertyId, BigDecimal amount, String currency, InvestmentStatus status,
                      BigDecimal interestPercent, String idempotencyKey, Instant now) {
        this.userId = userId; this.opportunityId = opportunityId; this.propertyId = propertyId; this.amount = amount; this.currency = currency;
        this.status = status; this.interestPercent = interestPercent; this.idempotencyKey = idempotencyKey; this.createdAt = now; this.updatedAt = now;
    }
}
