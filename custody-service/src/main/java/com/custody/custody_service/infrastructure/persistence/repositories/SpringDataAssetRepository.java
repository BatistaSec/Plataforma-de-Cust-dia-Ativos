package com.custody.custody_service.infrastructure.persistence.repositories;

import com.custody.custody_service.infrastructure.persistence.entities.AssetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataAssetRepository extends JpaRepository<AssetEntity, UUID> {
    Optional<AssetEntity> findByTicker(String ticker);
}
