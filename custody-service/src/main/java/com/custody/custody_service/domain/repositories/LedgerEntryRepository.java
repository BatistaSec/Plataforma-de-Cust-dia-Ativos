package com.custody.custody_service.domain.repositories;

import com.custody.custody_service.domain.models.LedgerEntry;

import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository {

    LedgerEntry save(LedgerEntry entry);

    List<LedgerEntry> findByAccountIdAndAssetId(UUID accountId, UUID assetId, int offset, int limit);

    long countByAccountIdAndAssetId(UUID accountId, UUID assetId);
}
