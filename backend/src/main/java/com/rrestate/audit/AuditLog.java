package com.rrestate.audit;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Entity
@Table(name = "audit_logs")
@Getter
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private UUID actorId;
    private String action;
    private String entityType;
    private String entityId;
    private String details;
    private String ipAddress;
    private String requestId;
    private Instant createdAt;

    protected AuditLog() {}

    AuditLog(UUID actorId, String action, String entityType, String entityId, String details, String ip, String requestId, Instant at) {
        this.actorId = actorId; this.action = action; this.entityType = entityType; this.entityId = entityId;
        this.details = details; this.ipAddress = ip; this.requestId = requestId; this.createdAt = at;
    }
}
