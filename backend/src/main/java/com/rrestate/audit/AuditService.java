package com.rrestate.audit;

import com.rrestate.common.RequestIdFilter;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** Append-only audit trail. Never pass secrets or personal documents in `details`. */
@Service
public class AuditService {
    private final AuditLogRepository repo;
    private final Clock clock;

    public AuditService(AuditLogRepository repo, Clock clock) { this.repo = repo; this.clock = clock; }

    public void record(UUID actorId, String action, String entityType, Object entityId, String details) {
        String ip = null;
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes s) ip = s.getRequest().getRemoteAddr();
        String d = details == null ? null : details.substring(0, Math.min(details.length(), 500));
        repo.save(new AuditLog(actorId, action, entityType, entityId == null ? null : entityId.toString(), d, ip, MDC.get(RequestIdFilter.MDC_KEY), Instant.now(clock)));
    }
}
