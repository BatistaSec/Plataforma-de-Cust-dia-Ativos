package com.custody.audit_service.domain.models;

import com.custody.audit_service.domain.enums.AuditEventType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modelo de domínio puro que representa um registro de auditoria.
 * Sem anotações de framework — regra de negócio pura.
 */
public class AuditLog {

    private UUID id;

    /** ID da ordem referenciada */
    private UUID orderId;

    /** ID do portfólio envolvido */
    private UUID portfolioId;

    /** ID do ativo envolvido */
    private UUID assetId;

    /** Tipo do evento que gerou esse registro */
    private AuditEventType eventType;

    /** Payload completo do evento em JSON para rastreabilidade */
    private String payload;

    /** Quando o evento foi registrado */
    private LocalDateTime occurredAt;

    public AuditLog() {}

    public AuditLog(UUID id, UUID orderId, UUID portfolioId, UUID assetId,
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
    public void setId(UUID id) { this.id = id; }

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }

    public UUID getPortfolioId() { return portfolioId; }
    public void setPortfolioId(UUID portfolioId) { this.portfolioId = portfolioId; }

    public UUID getAssetId() { return assetId; }
    public void setAssetId(UUID assetId) { this.assetId = assetId; }

    public AuditEventType getEventType() { return eventType; }
    public void setEventType(AuditEventType eventType) { this.eventType = eventType; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
}
