package com.rrestate.auth;

import com.rrestate.config.AppProperties;
import java.time.Clock;
import java.time.Instant;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Creates the first admin from ADMIN_EMAIL / ADMIN_PASSWORD when set and no such user exists. */
@Component
class AdminBootstrap implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AppProperties props;
    private final Clock clock;

    AdminBootstrap(UserRepository users, PasswordEncoder encoder, AppProperties props, Clock clock) {
        this.users = users; this.encoder = encoder; this.props = props; this.clock = clock;
    }

    @Override
    public void run(ApplicationArguments args) {
        var cfg = props.bootstrapAdmin();
        if (cfg.email().isBlank() || cfg.password().isBlank()) return;
        if (cfg.password().length() < 12) throw new IllegalStateException("ADMIN_PASSWORD must be at least 12 characters.");
        if (users.existsByEmail(cfg.email())) return;
        User admin = new User(cfg.email().trim().toLowerCase(), encoder.encode(cfg.password()), "Admin", "User", Set.of(Role.USER, Role.ADMIN), Instant.now(clock));
        admin.setKycStatus(KycStatus.VERIFIED);
        users.save(admin);
        log.info("Bootstrap admin account created.");
    }
}
