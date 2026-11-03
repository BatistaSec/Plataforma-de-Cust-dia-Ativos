package com.custody.custody_service.infrastructure.persistence.adapters;

import com.custody.custody_service.domain.models.AssetMovement;
import com.custody.custody_service.domain.repositories.AssetMovementRepository;
import com.custody.custody_service.infrastructure.mappers.AssetMovementMapper;
import com.custody.custody_service.infrastructure.persistence.repositories.SpringDataAssetMovementRepository;
import org.springframework.stereotype.Component;

@Component
public class AssetMovementRepositoryImpl implements AssetMovementRepository {

    private final SpringDataAssetMovementRepository springDataRepository;

    public AssetMovementRepositoryImpl(SpringDataAssetMovementRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public AssetMovement save(AssetMovement movement) {
        var entity = AssetMovementMapper.toEntity(movement);
        var saved = springDataRepository.save(entity);
        return AssetMovementMapper.toDomain(saved);
    }
}
