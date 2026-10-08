package com.custody.custody_service.infrastructure.persistence.adapters;

import com.custody.custody_service.domain.models.CustodyOrder;
import com.custody.custody_service.domain.repositories.CustodyOrderRepository;
import com.custody.custody_service.infrastructure.mappers.AssetMapper;
import com.custody.custody_service.infrastructure.mappers.CustodyOrderMapper;
import com.custody.custody_service.infrastructure.mappers.PortfolioMapper;
import com.custody.custody_service.infrastructure.persistence.entities.CustodyOrderEntity;
import com.custody.custody_service.infrastructure.persistence.repositories.SpringDataCustodyOrderRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class CustodyOrderRepositoryImpl implements CustodyOrderRepository {

    private final SpringDataCustodyOrderRepository springDataRepository;

    public CustodyOrderRepositoryImpl(SpringDataCustodyOrderRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public CustodyOrder save(CustodyOrder order) {
        CustodyOrderEntity entity = CustodyOrderMapper.toEntity(order);
        entity.setPortfolio(PortfolioMapper.toEntity(order.getPortfolio()));
        entity.setAsset(AssetMapper.toEntity(order.getAsset()));
        var saved = springDataRepository.save(entity);
        return CustodyOrderMapper.toDomain(saved);
    }

    @Override
    public Optional<CustodyOrder> findById(UUID id) {
        return springDataRepository.findById(id).map(CustodyOrderMapper::toDomain);
    }

    @Override
    public List<CustodyOrder> findByPortfolioId(UUID portfolioId) {
        return springDataRepository.findByPortfolioId(portfolioId).stream()
                .map(CustodyOrderMapper::toDomain)
                .collect(Collectors.toList());
    }
}
