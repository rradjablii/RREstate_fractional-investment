package com.rrestate.property;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

/** The raisable offering attached to a property. raisedAmount is only changed through atomic repository updates. */
@Entity
@Table(name = "investment_opportunities")
@Getter
public class Opportunity {
    @Id @GeneratedValue @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;
    private UUID propertyId;
    private BigDecimal minimumInvestment;
    private BigDecimal targetRaise;
    private BigDecimal raisedAmount = BigDecimal.ZERO;
    @Setter private boolean investmentEnabled;

    protected Opportunity() {}
    public Opportunity(UUID propertyId, BigDecimal minimumInvestment, BigDecimal targetRaise, boolean investmentEnabled) {
        this.propertyId = propertyId; this.minimumInvestment = minimumInvestment; this.targetRaise = targetRaise; this.investmentEnabled = investmentEnabled;
    }
}
