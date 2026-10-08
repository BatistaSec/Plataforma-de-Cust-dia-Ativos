package com.custody.custody_service.application.usecases;

import com.custody.custody_service.application.dto.AssetOutput;
import com.custody.custody_service.domain.models.Asset;
import com.custody.custody_service.domain.repositories.AssetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateAssetUseCase {

    private final AssetRepository assetRepository;

    public CreateAssetUseCase(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    @Transactional
    public AssetOutput execute(String ticker, String name) {
        Asset asset = new Asset(ticker, name);
        Asset saved = assetRepository.save(asset);
        return new AssetOutput(saved.getId(), saved.getTicker(), saved.getName());
    }
}
