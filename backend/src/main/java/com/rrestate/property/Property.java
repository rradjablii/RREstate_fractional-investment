package com.rrestate.property;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "properties")
@Getter
public class Property {
    @Id @GeneratedValue @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;
    private String name;
    private String country;
    private String city;
    private String address;
    private String propertyType;
    @Column(columnDefinition = "text")
    private String description;
    @Enumerated(EnumType.STRING) @Setter
    private PropertyStatus status;
    private String currency;
    private BigDecimal acquisitionPrice;
    private BigDecimal valuation;
    private BigDecimal expectedYieldPercent;
    private BigDecimal projectedNetIncome;
    private String legalStructure;
    private String entityName;
    private Instant createdAt;
    @Setter private Instant updatedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "property_id", nullable = false)
    @OrderBy("sortOrder")
    private List<PropertyImage> images = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "property_id", nullable = false)
    @OrderBy("createdAt")
    private List<PropertyDocument> documents = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "property_id", nullable = false)
    @OrderBy("sortOrder")
    private List<FinancialLine> financialLines = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "property_risks", joinColumns = @JoinColumn(name = "property_id"))
    @Column(name = "text") @OrderColumn(name = "sort_order")
    private List<String> risks = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "property_disclosures", joinColumns = @JoinColumn(name = "property_id"))
    @Column(name = "text") @OrderColumn(name = "sort_order")
    private List<String> disclosures = new ArrayList<>();

    protected Property() {}

    public Property(String name, String country, String city, String address, String propertyType, String description,
                    PropertyStatus status, String currency, BigDecimal acquisitionPrice, BigDecimal valuation,
                    BigDecimal expectedYieldPercent, BigDecimal projectedNetIncome, String legalStructure, String entityName, Instant now) {
        this.name = name; this.country = country; this.city = city; this.address = address; this.propertyType = propertyType;
        this.description = description; this.status = status; this.currency = currency; this.acquisitionPrice = acquisitionPrice;
        this.valuation = valuation; this.expectedYieldPercent = expectedYieldPercent; this.projectedNetIncome = projectedNetIncome;
        this.legalStructure = legalStructure; this.entityName = entityName; this.createdAt = now; this.updatedAt = now;
    }
}
