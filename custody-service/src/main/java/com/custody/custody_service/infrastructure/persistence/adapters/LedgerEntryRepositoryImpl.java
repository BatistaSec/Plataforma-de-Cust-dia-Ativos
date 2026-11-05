package com.custody.custody_service.infrastructure.persistence.adapters;

import com.custody.custody_service.domain.models.LedgerEntry;
import com.custody.custody_service.domain.repositories.LedgerEntryRepository;
import com.custody.custody_service.infrastructure.mappers.LedgerEntryMapper;
import com.custody.custody_service.infrastructure.persistence.repositories.SpringDataLedgerEntryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class LedgerEntryRepositoryImpl implements LedgerEntryRepository {

    private final SpringDataLedgerEntryRepository springDataRepository;

    public LedgerEntryRepositoryImpl(SpringDataLedgerEntryRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public LedgerEntry save(LedgerEntry entry) {
        var entity = LedgerEntryMapper.toEntity(entry);
        var saved = springDataRepository.save(entity);
        return LedgerEntryMapper.toDomain(saved);
    }

    @Override
    public List<LedgerEntry> findByAccountIdAndAssetId(UUID accountId, UUID assetId, int offset, int limit) {
        int page = offset / limit;
        if (assetId != null) {
            return springDataRepository
                    .findByAccountIdAndAssetIdOrderByCreatedAtDesc(accountId, assetId, PageRequest.of(page, limit))
                    .stream().map(LedgerEntryMapper::toDomain).collect(Collectors.toList());
        }
        return springDataRepository
                .findByAccountIdOrderByCreatedAtDesc(accountId, PageRequest.of(page, limit))
                .stream().map(LedgerEntryMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public long countByAccountIdAndAssetId(UUID accountId, UUID assetId) {
        if (assetId != null) {
            return springDataRepository
                    .findByAccountIdAndAssetIdOrderByCreatedAtDesc(accountId, assetId, PageRequest.of(0, 1))
                    .getTotalElements();
        }
        return springDataRepository
                .findByAccountIdOrderByCreatedAtDesc(accountId, PageRequest.of(0, 1))
                .getTotalElements();
    }
}
