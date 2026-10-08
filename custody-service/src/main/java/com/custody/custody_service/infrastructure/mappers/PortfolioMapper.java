package com.custody.custody_service.infrastructure.mappers;

import com.custody.custody_service.domain.models.Portfolio;
import com.custody.custody_service.domain.models.PortfolioAsset;
import com.custody.custody_service.infrastructure.persistence.entities.PortfolioAssetEntity;
import com.custody.custody_service.infrastructure.persistence.entities.PortfolioEntity;

import java.util.stream.Collectors;

public class PortfolioMapper {

    private PortfolioMapper() {}

    public static PortfolioEntity toEntity(Portfolio portfolio) {
        return new PortfolioEntity(portfolio.getId(), portfolio.getUserId());
    }

    public static Portfolio toDomain(PortfolioEntity entity) {
        Portfolio portfolio = new Portfolio(entity.getId(), entity.getUserId());
        if (entity.getAssets() != null) {
            var domainAssets = entity.getAssets().stream()
                    .map(paEntity -> {
                        var asset = AssetMapper.toDomain(paEntity.getAsset());
                        return new PortfolioAsset(
                                paEntity.getId(),
                                portfolio,
                                asset,
                                paEntity.getQuantity()
                        );
                    })
                    .collect(Collectors.toList());
            portfolio.setAssets(domainAssets);
        }
        return portfolio;
    }
}
