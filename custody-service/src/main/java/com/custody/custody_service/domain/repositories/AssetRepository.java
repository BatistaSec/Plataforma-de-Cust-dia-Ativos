package com.custody.custody_service.domain.repositories;

import com.custody.custody_service.domain.models.Asset;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssetRepository {

    Asset save(Asset asset);

    Optional<Asset> findById(UUID id);

    Optional<Asset> findByTicker(String ticker);

    List<Asset> findAll();
}
