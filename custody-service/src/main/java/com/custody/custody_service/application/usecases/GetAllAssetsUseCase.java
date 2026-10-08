package com.custody.custody_service.application.usecases;

import com.custody.custody_service.application.dto.AssetOutput;
import com.custody.custody_service.domain.models.Asset;
import com.custody.custody_service.domain.repositories.AssetRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GetAllAssetsUseCase {

    private final AssetRepository assetRepository;

    public GetAllAssetsUseCase(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    @Cacheable(value = "assets")
    @Transactional(readOnly = true)
    public List<AssetOutput> execute() {
        return assetRepository.findAll().stream()
                .map(asset -> new AssetOutput(asset.getId(), asset.getTicker(), asset.getName()))
                .collect(Collectors.toList());
    }
}
