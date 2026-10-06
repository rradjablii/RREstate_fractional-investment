package com.rrestate.portfolio;

import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/portfolio")
public class PortfolioController {
    private final PortfolioService service;

    public PortfolioController(PortfolioService service) { this.service = service; }

    /** The only portfolio endpoint: the owner is always the token subject. */
    @GetMapping("/me")
    PortfolioService.PortfolioResponse me(@AuthenticationPrincipal Jwt jwt, @RequestParam(required = false) String currency) {
        return service.forUser(UUID.fromString(jwt.getSubject()), currency != null && currency.matches("^[A-Z]{3}$") ? currency : null);
    }
}
