package com.rrestate.auth;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "refresh_tokens")
@Getter
public class RefreshToken {
    @Id @GeneratedValue @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;
    private UUID userId;
    private String tokenHash;
    private Instant expiresAt;
    @Setter private Instant revokedAt;
    private Instant createdAt;

    protected RefreshToken() {}

    public RefreshToken(UUID userId, String tokenHash, Instant expiresAt, Instant now) {
        this.userId = userId; this.tokenHash = tokenHash; this.expiresAt = expiresAt; this.createdAt = now;
    }
}
