package com.custody.custody_service.infrastructure.mappers;

import com.custody.custody_service.domain.models.AssetMovement;
import com.custody.custody_service.infrastructure.persistence.entities.AssetMovementEntity;

public class AssetMovementMapper {

    private AssetMovementMapper() {}

    public static AssetMovementEntity toEntity(AssetMovement movement) {
        AssetMovementEntity entity = new AssetMovementEntity();
        entity.setId(movement.getId());
        entity.setPortfolioId(movement.getPortfolioId());
        entity.setAssetId(movement.getAssetId());
        entity.setType(movement.getType());
        entity.setQuantity(movement.getQuantity());
        entity.setReferenceOrderId(movement.getReferenceOrderId());
        entity.setOccurredAt(movement.getOccurredAt());
        return entity;
    }

    public static AssetMovement toDomain(AssetMovementEntity entity) {
        return new AssetMovement(
                entity.getId(),
                entity.getPortfolioId(),
                entity.getAssetId(),
                entity.getType(),
                entity.getQuantity(),
                entity.getReferenceOrderId(),
                entity.getOccurredAt()
        );
    }
}
