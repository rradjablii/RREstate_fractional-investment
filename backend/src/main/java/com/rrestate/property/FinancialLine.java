package com.rrestate.property;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "property_financial_lines")
@Getter
public class FinancialLine {
    @Id @GeneratedValue @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;
    private String label;
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private FinancialBasis basis;
    private int sortOrder;

    protected FinancialLine() {}
    public FinancialLine(String label, BigDecimal amount, FinancialBasis basis, int sortOrder) {
        this.label = label; this.amount = amount; this.basis = basis; this.sortOrder = sortOrder;
    }
}
