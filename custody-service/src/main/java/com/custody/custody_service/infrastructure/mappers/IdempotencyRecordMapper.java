package com.custody.custody_service.infrastructure.mappers;

import com.custody.custody_service.domain.models.IdempotencyRecord;
import com.custody.custody_service.infrastructure.persistence.entities.IdempotencyRecordEntity;

public class IdempotencyRecordMapper {

    private IdempotencyRecordMapper() {}

    public static IdempotencyRecordEntity toEntity(IdempotencyRecord record) {
        IdempotencyRecordEntity entity = new IdempotencyRecordEntity();
        entity.setId(record.getId());
        entity.setIdempotencyKey(record.getIdempotencyKey());
        entity.setRequestHash(record.getRequestHash());
        entity.setResponseSnapshot(record.getResponseSnapshot());
        entity.setCreatedAt(record.getCreatedAt());
        return entity;
    }

    public static IdempotencyRecord toDomain(IdempotencyRecordEntity entity) {
        return new IdempotencyRecord(
                entity.getId(),
                entity.getIdempotencyKey(),
                entity.getRequestHash(),
                entity.getResponseSnapshot(),
                entity.getCreatedAt()
        );
    }
}
