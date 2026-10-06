package com.rrestate.distribution;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "distribution_allocations")
@Getter
public class Allocation {
    @Id @GeneratedValue @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;
    private UUID distributionId;
    private UUID userId;
    private UUID positionId;
    private BigDecimal amount;
    private String currency;
    @Setter private String status;
    @Setter private Instant paidAt;

    protected Allocation() {}

    public Allocation(UUID distributionId, UUID userId, UUID positionId, BigDecimal amount, String currency) {
        this.distributionId = distributionId; this.userId = userId; this.positionId = positionId; this.amount = amount;
        this.currency = currency; this.status = "PENDING";
    }
}
