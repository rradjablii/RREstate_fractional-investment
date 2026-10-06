package com.rrestate.property;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class PropertyDtos {
    private PropertyDtos() {}

    public record PropertySummary(UUID id, String name, String country, String city, String propertyType, PropertyStatus status,
                                  String coverImageUrl, String currency, String expectedYieldPercent, String minimumInvestment,
                                  String targetRaise, String raisedAmount, String fundingProgressPercent) {}

    public record FinancialLineDto(String label, String amount, FinancialBasis basis) {}
    public record ImageDto(UUID id, String url, String caption) {}
    public record DocumentRef(UUID id, String title, String category, Long sizeBytes) {}

    public record PropertyDetail(UUID id, String name, String country, String city, String propertyType, PropertyStatus status,
                                 String coverImageUrl, String currency, String expectedYieldPercent, String minimumInvestment,
                                 String targetRaise, String raisedAmount, String fundingProgressPercent,
                                 String description, String address, String acquisitionPrice, String valuation, UUID opportunityId,
                                 boolean investmentEnabled, String legalStructure, String entityName,
                                 List<FinancialLineDto> financials, String projectedNetIncome, List<ImageDto> images,
                                 List<String> risks, List<String> disclosures, List<DocumentRef> documents) {}

    // ---- admin requests ----

    public record FinancialLineInput(@NotBlank @Size(max = 120) String label, @NotNull @Digits(integer = 15, fraction = 2) BigDecimal amount, @NotNull FinancialBasis basis) {}
    public record ImageInput(@NotBlank @Size(max = 1000) @Pattern(regexp = "^https://.+", message = "must be an https URL") String url, @Size(max = 200) String caption) {}
    public record DocumentInput(@NotBlank @Size(max = 200) String title, @NotBlank @Size(max = 40) String category) {}

    public record CreatePropertyRequest(
            @NotBlank @Size(max = 200) String name,
            @NotBlank @Size(max = 80) String country,
            @NotBlank @Size(max = 120) String city,
            @Size(max = 300) String address,
            @NotBlank @Size(max = 60) String propertyType,
            @NotBlank @Size(max = 10000) String description,
            @NotNull PropertyStatus status,
            @NotBlank @Pattern(regexp = "^[A-Z]{3}$", message = "must be an ISO 4217 code") String currency,
            @NotNull @Positive @Digits(integer = 15, fraction = 2) BigDecimal acquisitionPrice,
            @Positive @Digits(integer = 15, fraction = 2) BigDecimal valuation,
            @NotNull @DecimalMin("0.0") @DecimalMax("100.0") @Digits(integer = 3, fraction = 3) BigDecimal expectedYieldPercent,
            @Digits(integer = 15, fraction = 2) BigDecimal projectedNetIncome,
            @Size(max = 200) String legalStructure,
            @Size(max = 200) String entityName,
            @NotNull @Positive @Digits(integer = 15, fraction = 2) BigDecimal minimumInvestment,
            @NotNull @Positive @Digits(integer = 15, fraction = 2) BigDecimal targetRaise,
            boolean investmentEnabled,
            @Valid @Size(max = 30) List<ImageInput> images,
            @Valid @Size(max = 50) List<DocumentInput> documents,
            @Valid @Size(max = 50) List<FinancialLineInput> financials,
            @Size(max = 30) List<@NotBlank @Size(max = 1000) String> risks,
            @Size(max = 30) List<@NotBlank @Size(max = 2000) String> disclosures) {}

    public record UpdateStatusRequest(@NotNull PropertyStatus status) {}
}
