package com.custody.custody_service.infrastructure.persistence.adapters;

import com.custody.custody_service.domain.models.IdempotencyRecord;
import com.custody.custody_service.domain.repositories.IdempotencyRecordRepository;
import com.custody.custody_service.infrastructure.mappers.IdempotencyRecordMapper;
import com.custody.custody_service.infrastructure.persistence.repositories.SpringDataIdempotencyRecordRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class IdempotencyRecordRepositoryImpl implements IdempotencyRecordRepository {

    private final SpringDataIdempotencyRecordRepository springDataRepository;

    public IdempotencyRecordRepositoryImpl(SpringDataIdempotencyRecordRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public IdempotencyRecord save(IdempotencyRecord record) {
        var entity = IdempotencyRecordMapper.toEntity(record);
        var saved = springDataRepository.save(entity);
        return IdempotencyRecordMapper.toDomain(saved);
    }

    @Override
    public Optional<IdempotencyRecord> findByIdempotencyKey(UUID idempotencyKey) {
        return springDataRepository.findByIdempotencyKey(idempotencyKey).map(IdempotencyRecordMapper::toDomain);
    }
}
