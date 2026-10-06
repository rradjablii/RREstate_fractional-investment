package com.rrestate.distribution;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "distributions")
@Getter
public class Distribution {
    @Id @GeneratedValue @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;
    private UUID propertyId;
    private BigDecimal totalAmount;
    private String currency;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    @Enumerated(EnumType.STRING) @Setter
    private DistributionStatus status;
    private UUID createdBy;
    private Instant createdAt;
    @Setter private Instant approvedAt;

    protected Distribution() {}

    public Distribution(UUID propertyId, BigDecimal totalAmount, String currency, LocalDate start, LocalDate end, UUID createdBy, Instant now) {
        this.propertyId = propertyId; this.totalAmount = totalAmount; this.currency = currency; this.periodStart = start; this.periodEnd = end;
        this.status = DistributionStatus.CREATED; this.createdBy = createdBy; this.createdAt = now;
    }
}
