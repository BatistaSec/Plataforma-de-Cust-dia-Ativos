package com.custody.custody_service.infrastructure.mappers;

import com.custody.custody_service.domain.models.LedgerEntry;
import com.custody.custody_service.infrastructure.persistence.entities.LedgerEntryEntity;

public class LedgerEntryMapper {

    private LedgerEntryMapper() {}

    public static LedgerEntryEntity toEntity(LedgerEntry entry) {
        LedgerEntryEntity entity = new LedgerEntryEntity();
        entity.setId(entry.getId());
        entity.setTransactionId(entry.getTransactionId());
        entity.setAccountId(entry.getAccountId());
        entity.setAssetId(entry.getAssetId());
        entity.setEntryType(entry.getEntryType());
        entity.setQuantity(entry.getQuantity());
        entity.setCreatedAt(entry.getCreatedAt());
        return entity;
    }

    public static LedgerEntry toDomain(LedgerEntryEntity entity) {
        return new LedgerEntry(
                entity.getId(),
                entity.getTransactionId(),
                entity.getAccountId(),
                entity.getAssetId(),
                entity.getEntryType(),
                entity.getQuantity(),
                entity.getCreatedAt()
        );
    }
}
