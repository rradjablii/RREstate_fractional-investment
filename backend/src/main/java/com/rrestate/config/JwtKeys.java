package com.rrestate.config;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

/** RSA signing keys come from deployment config; an ephemeral pair is generated only in dev/test. */
@Configuration
public class JwtKeys {
    private static final Logger log = LoggerFactory.getLogger(JwtKeys.class);

    @Bean
    KeyPair jwtKeyPair(AppProperties props, Environment env) throws Exception {
        AppProperties.Jwt jwt = props.jwt();
        boolean configured = !jwt.privateKey().isBlank() && !jwt.publicKey().isBlank();
        if (configured) {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            RSAPrivateKey priv = (RSAPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(decode(jwt.privateKey())));
            RSAPublicKey pub = (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(decode(jwt.publicKey())));
            return new KeyPair(pub, priv);
        }
        if (!env.acceptsProfiles(Profiles.of("dev", "test"))) {
            throw new IllegalStateException("JWT_PRIVATE_KEY and JWT_PUBLIC_KEY must be configured outside dev/test.");
        }
        log.warn("Generating an ephemeral JWT key pair (dev/test only); tokens will not survive a restart.");
        KeyPairGenerator g = KeyPairGenerator.getInstance("RSA");
        g.initialize(2048);
        return g.generateKeyPair();
    }

    private static byte[] decode(String pem) {
        return Base64.getMimeDecoder().decode(pem.replaceAll("-----[A-Z ]+-----", "").replaceAll("\\s", ""));
    }
}
