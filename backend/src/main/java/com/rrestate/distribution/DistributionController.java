package com.rrestate.distribution;

import com.rrestate.common.PageResponse;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/distributions")
public class DistributionController {
    private final DistributionService service;

    public DistributionController(DistributionService service) { this.service = service; }

    @GetMapping
    PageResponse<DistributionService.DistributionResponse> list(@AuthenticationPrincipal Jwt jwt, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.listForUser(UUID.fromString(jwt.getSubject()), page, size);
    }
}
