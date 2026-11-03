package com.custody.custody_service.infrastructure.mappers;

import com.custody.custody_service.domain.models.PortfolioAsset;
import com.custody.custody_service.infrastructure.persistence.entities.PortfolioAssetEntity;

public class PortfolioAssetMapper {

    private PortfolioAssetMapper() {}

    public static PortfolioAssetEntity toEntity(PortfolioAsset domain) {
        PortfolioAssetEntity entity = new PortfolioAssetEntity();
        entity.setId(domain.getId());
        entity.setPortfolio(PortfolioMapper.toEntity(domain.getPortfolio()));
        entity.setAsset(AssetMapper.toEntity(domain.getAsset()));
        entity.setQuantity(domain.getQuantity());
        entity.setVersion(domain.getVersion());
        return entity;
    }

    public static PortfolioAsset toDomain(PortfolioAssetEntity entity) {
        return new PortfolioAsset(
                entity.getId(),
                PortfolioMapper.toDomain(entity.getPortfolio()),
                AssetMapper.toDomain(entity.getAsset()),
                entity.getQuantity(),
                entity.getVersion()
        );
    }
}
