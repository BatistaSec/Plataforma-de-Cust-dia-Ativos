package com.custody.custody_service.infrastructure.mappers;

import com.custody.custody_service.domain.models.CustodyOrder;
import com.custody.custody_service.infrastructure.persistence.entities.CustodyOrderEntity;

public class CustodyOrderMapper {

    private CustodyOrderMapper() {}

    public static CustodyOrderEntity toEntity(CustodyOrder order) {
        CustodyOrderEntity entity = new CustodyOrderEntity();
        entity.setId(order.getId());
        entity.setType(order.getType());
        entity.setQuantity(order.getQuantity());
        entity.setStatus(order.getStatus());
        // Portfolio e Asset entities devem ser resolvidos no adapter
        return entity;
    }

    public static CustodyOrder toDomain(CustodyOrderEntity entity) {
        var portfolio = PortfolioMapper.toDomain(entity.getPortfolio());
        var asset = AssetMapper.toDomain(entity.getAsset());

        return new CustodyOrder(
                entity.getId(),
                portfolio,
                asset,
                entity.getType(),
                entity.getQuantity(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
