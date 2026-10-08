package com.custody.custody_service.infrastructure.persistence.adapters;

import com.custody.custody_service.domain.models.Asset;
import com.custody.custody_service.domain.repositories.AssetRepository;
import com.custody.custody_service.infrastructure.mappers.AssetMapper;
import com.custody.custody_service.infrastructure.persistence.repositories.SpringDataAssetRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class AssetRepositoryImpl implements AssetRepository {

    private final SpringDataAssetRepository springDataRepository;

    public AssetRepositoryImpl(SpringDataAssetRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Asset save(Asset asset) {
        var entity = AssetMapper.toEntity(asset);
        var saved = springDataRepository.save(entity);
        return AssetMapper.toDomain(saved);
    }

    @Override
    public Optional<Asset> findById(UUID id) {
        return springDataRepository.findById(id).map(AssetMapper::toDomain);
    }

    @Override
    public Optional<Asset> findByTicker(String ticker) {
        return springDataRepository.findByTicker(ticker).map(AssetMapper::toDomain);
    }

    @Override
    public List<Asset> findAll() {
        return springDataRepository.findAll().stream()
                .map(AssetMapper::toDomain)
                .collect(Collectors.toList());
    }
}
