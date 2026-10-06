package com.rrestate.investment;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class InvestmentDtos {
    private InvestmentDtos() {}

    public record CreateInvestmentRequest(
            @NotNull UUID opportunityId,
            @NotNull @Positive @Digits(integer = 15, fraction = 2, message = "must have at most 2 decimal places") BigDecimal amount,
            @NotBlank @Pattern(regexp = "^[A-Z]{3}$", message = "must be an ISO 4217 code") String currency) {}

    public record InvestmentResponse(UUID id, UUID propertyId, String propertyName, String amount, String currency, InvestmentStatus status,
                                     String interestPercent, Instant investmentDate, Instant createdAt) {}

    public record AdminInvestmentResponse(UUID id, UUID propertyId, String propertyName, String amount, String currency, InvestmentStatus status,
                                          String interestPercent, Instant investmentDate, Instant createdAt, String userEmail) {}
}
