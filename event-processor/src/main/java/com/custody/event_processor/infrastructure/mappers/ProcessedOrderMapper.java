package com.custody.event_processor.infrastructure.mappers;

import com.custody.event_processor.domain.models.ProcessedOrder;
import com.custody.event_processor.infrastructure.persistence.entities.ProcessedOrderEntity;
import org.springframework.stereotype.Component;

@Component
public class ProcessedOrderMapper {

    public ProcessedOrderEntity toEntity(ProcessedOrder domain) {
        if (domain == null) {
            return null;
        }
        ProcessedOrderEntity entity = new ProcessedOrderEntity();
        entity.setId(domain.getId());
        entity.setOrderId(domain.getOrderId());
        entity.setPortfolioId(domain.getPortfolioId());
        entity.setAssetId(domain.getAssetId());
        entity.setType(domain.getType());
        entity.setQuantity(domain.getQuantity());
        entity.setStatus(domain.getStatus());
        entity.setProcessedAt(domain.getProcessedAt());
        return entity;
    }

    public ProcessedOrder toDomain(ProcessedOrderEntity entity) {
        if (entity == null) {
            return null;
        }
        ProcessedOrder domain = new ProcessedOrder();
        domain.setId(entity.getId());
        domain.setOrderId(entity.getOrderId());
        domain.setPortfolioId(entity.getPortfolioId());
        domain.setAssetId(entity.getAssetId());
        domain.setType(entity.getType());
        domain.setQuantity(entity.getQuantity());
        domain.setStatus(entity.getStatus());
        domain.setProcessedAt(entity.getProcessedAt());
        return domain;
    }
}
