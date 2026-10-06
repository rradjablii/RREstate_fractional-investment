package com.rrestate.auth;

import com.rrestate.config.AppProperties;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private static final String COOKIE_PATH = "/api/v1/auth";

    private final AuthService auth;
    private final AppProperties props;

    public AuthController(AuthService auth, AppProperties props) { this.auth = auth; this.props = props; }

    @PostMapping("/register")
    ResponseEntity<AuthDtos.AuthResponse> register(@Valid @RequestBody AuthDtos.RegisterRequest req) {
        return respond(HttpStatus.CREATED, auth.register(req));
    }

    @PostMapping("/login")
    ResponseEntity<AuthDtos.AuthResponse> login(@Valid @RequestBody AuthDtos.LoginRequest req) {
        return respond(HttpStatus.OK, auth.login(req));
    }

    @PostMapping("/refresh")
    ResponseEntity<AuthDtos.AuthResponse> refresh(@CookieValue(name = "rrestate_refresh", required = false) String token) {
        return respond(HttpStatus.OK, auth.refresh(token));
    }

    @PostMapping("/logout")
    ResponseEntity<Void> logout(@CookieValue(name = "rrestate_refresh", required = false) String token) {
        auth.logout(token);
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookie("", 0).toString()).build();
    }

    private ResponseEntity<AuthDtos.AuthResponse> respond(HttpStatus status, AuthService.Issued issued) {
        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, cookie(issued.refreshToken(), props.refresh().ttl().toSeconds()).toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(issued.response());
    }

    private ResponseCookie cookie(String value, long maxAgeSeconds) {
        return ResponseCookie.from(props.refresh().cookieName(), value)
                .httpOnly(true).secure(props.refresh().cookieSecure()).sameSite("Strict")
                .path(COOKIE_PATH).maxAge(maxAgeSeconds).build();
    }
}
