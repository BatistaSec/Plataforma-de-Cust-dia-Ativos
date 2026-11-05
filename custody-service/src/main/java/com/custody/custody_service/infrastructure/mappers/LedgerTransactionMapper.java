package com.custody.custody_service.infrastructure.mappers;

import com.custody.custody_service.domain.models.LedgerTransaction;
import com.custody.custody_service.infrastructure.persistence.entities.LedgerTransactionEntity;

public class LedgerTransactionMapper {

    private LedgerTransactionMapper() {}

    public static LedgerTransactionEntity toEntity(LedgerTransaction tx) {
        LedgerTransactionEntity entity = new LedgerTransactionEntity();
        entity.setId(tx.getId());
        entity.setReference(tx.getReference());
        entity.setTransactionType(tx.getTransactionType());
        entity.setStatus(tx.getStatus());
        entity.setCreatedAt(tx.getCreatedAt());
        return entity;
    }

    public static LedgerTransaction toDomain(LedgerTransactionEntity entity) {
        return new LedgerTransaction(
                entity.getId(),
                entity.getReference(),
                entity.getTransactionType(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}
