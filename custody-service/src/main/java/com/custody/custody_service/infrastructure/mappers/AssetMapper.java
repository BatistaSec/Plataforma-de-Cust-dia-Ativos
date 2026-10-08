package com.custody.custody_service.infrastructure.mappers;

import com.custody.custody_service.domain.models.Asset;
import com.custody.custody_service.infrastructure.persistence.entities.AssetEntity;

public class AssetMapper {

    private AssetMapper() {}

    public static AssetEntity toEntity(Asset asset) {
        return new AssetEntity(asset.getId(), asset.getTicker(), asset.getName());
    }

    public static Asset toDomain(AssetEntity entity) {
        return new Asset(entity.getId(), entity.getTicker(), entity.getName());
    }
}
