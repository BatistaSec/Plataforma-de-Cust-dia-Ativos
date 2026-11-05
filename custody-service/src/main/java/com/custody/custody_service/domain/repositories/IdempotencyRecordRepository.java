package com.custody.custody_service.domain.repositories;

import com.custody.custody_service.domain.models.IdempotencyRecord;

import java.util.Optional;
import java.util.UUID;

public interface IdempotencyRecordRepository {

    IdempotencyRecord save(IdempotencyRecord record);

    Optional<IdempotencyRecord> findByIdempotencyKey(UUID idempotencyKey);
}
