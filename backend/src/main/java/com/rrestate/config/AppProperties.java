package com.rrestate.config;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("rrestate")
public record AppProperties(Jwt jwt, Refresh refresh, Cors cors, RateLimit rateLimit,
                            Compliance compliance, BootstrapAdmin bootstrapAdmin, Pagination pagination) {

    public record Jwt(String issuer, String audience, Duration accessTtl, String privateKey, String publicKey) {}

    public record Refresh(Duration ttl, String cookieName, boolean cookieSecure) {}

    public record Cors(List<String> allowedOrigins) {
        public Cors { allowedOrigins = allowedOrigins == null ? List.of() : allowedOrigins.stream().filter(s -> !s.isBlank()).toList(); }
    }

    public record RateLimit(int authPerMinute, int investmentsPerMinute) {}

    /** Compliance gates. Investing stays off until the legal structure is approved. */
    public record Compliance(boolean investmentEnabled, boolean kycRequired) {}

    public record BootstrapAdmin(String email, String password) {}

    public record Pagination(int maxSize) {}
}
