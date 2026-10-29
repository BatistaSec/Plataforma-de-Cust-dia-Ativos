package com.custody.audit_service.infrastructure.mappers;

import com.custody.audit_service.domain.models.AuditLog;
import com.custody.audit_service.infrastructure.persistence.entities.AuditLogEntity;

public class AuditLogMapper {

    private AuditLogMapper() {}

    public static AuditLogEntity toEntity(AuditLog domain) {
        AuditLogEntity entity = new AuditLogEntity();
        entity.setId(domain.getId());
        entity.setOrderId(domain.getOrderId());
        entity.setPortfolioId(domain.getPortfolioId());
        entity.setAssetId(domain.getAssetId());
        entity.setEventType(domain.getEventType());
        entity.setPayload(domain.getPayload());
        entity.setOccurredAt(domain.getOccurredAt());
        return entity;
    }

    public static AuditLog toDomain(AuditLogEntity entity) {
        return new AuditLog(
                entity.getId(),
                entity.getOrderId(),
                entity.getPortfolioId(),
                entity.getAssetId(),
                entity.getEventType(),
                entity.getPayload(),
                entity.getOccurredAt()
        );
    }
}
