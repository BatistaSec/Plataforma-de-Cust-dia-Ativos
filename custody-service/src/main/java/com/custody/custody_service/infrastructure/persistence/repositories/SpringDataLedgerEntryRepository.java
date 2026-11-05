package com.custody.custody_service.infrastructure.persistence.repositories;

import com.custody.custody_service.infrastructure.persistence.entities.LedgerEntryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataLedgerEntryRepository extends JpaRepository<LedgerEntryEntity, UUID> {
    Page<LedgerEntryEntity> findByAccountIdAndAssetIdOrderByCreatedAtDesc(UUID accountId, UUID assetId, Pageable pageable);

    Page<LedgerEntryEntity> findByAccountIdOrderByCreatedAtDesc(UUID accountId, Pageable pageable);
}
