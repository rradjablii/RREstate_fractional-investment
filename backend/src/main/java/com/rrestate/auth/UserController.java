package com.rrestate.auth;

import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final AuthService auth;

    public UserController(AuthService auth) { this.auth = auth; }

    @GetMapping("/me")
    AuthDtos.UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        return AuthDtos.UserResponse.from(auth.requireUser(UUID.fromString(jwt.getSubject())));
    }
}
