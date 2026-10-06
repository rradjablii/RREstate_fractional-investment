package com.rrestate.auth;

import com.rrestate.audit.AuditService;
import com.rrestate.common.ApiException;
import com.rrestate.config.AppProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    /** Result of issuing credentials: the access token body plus the raw refresh token for the cookie. */
    public record Issued(AuthDtos.AuthResponse response, String refreshToken) {}

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder encoder;
    private final JwtEncoder jwtEncoder;
    private final AppProperties props;
    private final AuditService audit;
    private final Clock clock;
    /** Compared against when the email is unknown so timing does not reveal account existence. */
    private final String dummyHash;

    public AuthService(UserRepository users, RefreshTokenRepository refreshTokens, PasswordEncoder encoder, JwtEncoder jwtEncoder,
                       AppProperties props, AuditService audit, Clock clock) {
        this.users = users; this.refreshTokens = refreshTokens; this.encoder = encoder; this.jwtEncoder = jwtEncoder;
        this.props = props; this.audit = audit; this.clock = clock;
        this.dummyHash = encoder.encode("dummy-password-for-timing");
    }

    @Transactional
    public Issued register(AuthDtos.RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (users.existsByEmail(email)) throw ApiException.conflict("EMAIL_TAKEN", "An account with this email already exists.");
        User user;
        try {
            user = users.saveAndFlush(new User(email, encoder.encode(req.password()), req.firstName().trim(), req.lastName().trim(), Set.of(Role.USER), Instant.now(clock)));
        } catch (DataIntegrityViolationException e) {
            throw ApiException.conflict("EMAIL_TAKEN", "An account with this email already exists.");
        }
        audit.record(user.getId(), "USER_REGISTERED", "User", user.getId(), null);
        return issue(user);
    }

    @Transactional
    public Issued login(AuthDtos.LoginRequest req) {
        User user = users.findByEmail(req.email().trim()).orElse(null);
        boolean ok = encoder.matches(req.password(), user != null ? user.getPasswordHash() : dummyHash) && user != null && user.isEnabled();
        if (!ok) {
            audit.record(user != null ? user.getId() : null, "LOGIN_FAILED", "User", user != null ? user.getId() : null, null);
            throw ApiException.unauthorized("INVALID_CREDENTIALS", "Incorrect email or password.");
        }
        audit.record(user.getId(), "LOGIN", "User", user.getId(), null);
        return issue(user);
    }

    /** Rotates the refresh token. Presenting an already-revoked token revokes the whole family (theft signal). */
    @Transactional(noRollbackFor = ApiException.class)
    public Issued refresh(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) throw ApiException.unauthorized("UNAUTHENTICATED", "Session expired.");
        Instant now = Instant.now(clock);
        RefreshToken token = refreshTokens.findByTokenHash(hash(rawToken)).orElseThrow(() -> ApiException.unauthorized("UNAUTHENTICATED", "Session expired."));
        if (token.getRevokedAt() != null) {
            refreshTokens.revokeAllForUser(token.getUserId(), now);
            audit.record(token.getUserId(), "REFRESH_TOKEN_REUSE", "User", token.getUserId(), null);
            throw ApiException.unauthorized("UNAUTHENTICATED", "Session expired.");
        }
        if (token.getExpiresAt().isBefore(now)) throw ApiException.unauthorized("UNAUTHENTICATED", "Session expired.");
        User user = users.findById(token.getUserId()).filter(User::isEnabled).orElseThrow(() -> ApiException.unauthorized("UNAUTHENTICATED", "Session expired."));
        token.setRevokedAt(now);
        return issue(user);
    }

    @Transactional
    public void logout(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) return;
        refreshTokens.findByTokenHash(hash(rawToken)).ifPresent(t -> { if (t.getRevokedAt() == null) t.setRevokedAt(Instant.now(clock)); });
    }

    @Transactional(readOnly = true)
    public User requireUser(UUID id) {
        return users.findById(id).orElseThrow(() -> ApiException.unauthorized("UNAUTHENTICATED", "Account not found."));
    }

    private Issued issue(User user) {
        Instant now = Instant.now(clock);
        var ttl = props.jwt().accessTtl();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(props.jwt().issuer()).audience(List.of(props.jwt().audience()))
                .subject(user.getId().toString()).issuedAt(now).notBefore(now).expiresAt(now.plus(ttl))
                .claim("roles", user.getRoles().stream().map(Enum::name).sorted().toList())
                .build();
        String access = jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(SignatureAlgorithm.RS256).build(), claims)).getTokenValue();

        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        refreshTokens.save(new RefreshToken(user.getId(), hash(raw), now.plus(props.refresh().ttl()), now));
        return new Issued(new AuthDtos.AuthResponse(access, ttl.toSeconds(), AuthDtos.UserResponse.from(user)), raw);
    }

    static String hash(String raw) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
