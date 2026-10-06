package com.rrestate.portfolio;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "investor_positions")
@Getter
public class Position {
    @Id @GeneratedValue @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;
    private UUID userId;
    private UUID propertyId;
    private BigDecimal investedAmount;
    private BigDecimal interestPercent;
    private String currency;
    private Instant createdAt;
    private Instant updatedAt;

    protected Position() {}

    public Position(UUID userId, UUID propertyId, String currency, Instant now) {
        this.userId = userId; this.propertyId = propertyId; this.currency = currency;
        this.investedAmount = BigDecimal.ZERO; this.interestPercent = BigDecimal.ZERO; this.createdAt = now; this.updatedAt = now;
    }

    public void add(BigDecimal amount, BigDecimal interest, Instant now) {
        this.investedAmount = investedAmount.add(amount);
        this.interestPercent = interestPercent.add(interest);
        this.updatedAt = now;
    }
}
