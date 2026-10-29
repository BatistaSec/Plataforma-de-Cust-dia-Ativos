package com.custody.audit_service.application.dto;

import com.custody.audit_service.domain.enums.AuditEventType;

import java.time.LocalDateTime;
import java.util.UUID;

public class AuditLogOutput {

    private UUID id;
    private UUID orderId;
    private UUID portfolioId;
    private UUID assetId;
    private AuditEventType eventType;
    private String payload;
    private LocalDateTime occurredAt;

    public AuditLogOutput(UUID id, UUID orderId, UUID portfolioId, UUID assetId,
                          AuditEventType eventType, String payload, LocalDateTime occurredAt) {
        this.id = id;
        this.orderId = orderId;
        this.portfolioId = portfolioId;
        this.assetId = assetId;
        this.eventType = eventType;
        this.payload = payload;
        this.occurredAt = occurredAt;
    }

    public UUID getId() { return id; }
    public UUID getOrderId() { return orderId; }
    public UUID getPortfolioId() { return portfolioId; }
    public UUID getAssetId() { return assetId; }
    public AuditEventType getEventType() { return eventType; }
    public String getPayload() { return payload; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
}
