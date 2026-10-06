package com.rrestate.admin;

import com.rrestate.audit.AuditLog;
import com.rrestate.audit.AuditLogRepository;
import com.rrestate.common.PageResponse;
import com.rrestate.common.Paging;
import com.rrestate.distribution.DistributionService;
import com.rrestate.investment.InvestmentDtos.AdminInvestmentResponse;
import com.rrestate.investment.InvestmentService;
import com.rrestate.property.PropertyDtos.*;
import com.rrestate.property.PropertyService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * All routes require a staff role (SecurityConfig). Mutations are further restricted per method.
 * ADMIN/OPERATOR: properties, investment payment outcome. ADMIN only: distribution approval/payment.
 * ADMIN/COMPLIANCE: audit log.
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    public record AuditEntry(Long id, UUID actorId, String action, String entityType, String entityId, String details, String requestId, Instant createdAt) {}

    private final PropertyService properties;
    private final InvestmentService investments;
    private final DistributionService distributions;
    private final AuditLogRepository auditLogs;
    private final Paging paging;

    public AdminController(PropertyService properties, InvestmentService investments, DistributionService distributions, AuditLogRepository auditLogs, Paging paging) {
        this.properties = properties; this.investments = investments; this.distributions = distributions; this.auditLogs = auditLogs; this.paging = paging;
    }

    private static UUID actor(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }

    @GetMapping("/properties")
    PageResponse<PropertySummary> listProperties(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return properties.listAll(page, size);
    }

    @PostMapping("/properties")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    PropertyDetail createProperty(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreatePropertyRequest req) {
        return properties.create(actor(jwt), req);
    }

    @PutMapping("/properties/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    void updateStatus(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @Valid @RequestBody UpdateStatusRequest req) {
        properties.updateStatus(actor(jwt), id, req.status());
    }

    @GetMapping("/investments")
    PageResponse<AdminInvestmentResponse> listInvestments(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return investments.listAll(page, size);
    }

    @PostMapping("/investments/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    void confirm(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) { investments.confirmPayment(actor(jwt), id); }

    @PostMapping("/investments/{id}/fail")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    void fail(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) { investments.failPayment(actor(jwt), id); }

    @GetMapping("/distributions")
    PageResponse<DistributionService.AdminDistributionResponse> listDistributions(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return distributions.listAll(page, size);
    }

    @PostMapping("/distributions")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    DistributionService.AdminDistributionResponse createDistribution(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody DistributionService.CreateDistributionRequest req) {
        return distributions.create(actor(jwt), req);
    }

    @PostMapping("/distributions/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    void approve(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) { distributions.approve(actor(jwt), id); }

    @PostMapping("/distributions/{id}/mark-paid")
    @PreAuthorize("hasRole('ADMIN')")
    void markPaid(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) { distributions.markPaid(actor(jwt), id); }

    @GetMapping("/audit-logs")
    @PreAuthorize("hasAnyRole('ADMIN','COMPLIANCE')")
    PageResponse<AuditEntry> audit(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size) {
        return PageResponse.of(auditLogs.findAll(paging.of(page, size, Sort.by(Sort.Direction.DESC, "id"))),
                (AuditLog a) -> new AuditEntry(a.getId(), a.getActorId(), a.getAction(), a.getEntityType(), a.getEntityId(), a.getDetails(), a.getRequestId(), a.getCreatedAt()));
    }
}
