package com.custody.event_processor.infrastructure.persistence.adapters;

import com.custody.event_processor.domain.models.ProcessedOrder;
import com.custody.event_processor.domain.repositories.ProcessedOrderRepository;
import com.custody.event_processor.infrastructure.mappers.ProcessedOrderMapper;
import com.custody.event_processor.infrastructure.persistence.entities.ProcessedOrderEntity;
import com.custody.event_processor.infrastructure.persistence.repositories.SpringDataProcessedOrderRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class ProcessedOrderRepositoryImpl implements ProcessedOrderRepository {

    private final SpringDataProcessedOrderRepository springDataRepository;
    private final ProcessedOrderMapper mapper;

    public ProcessedOrderRepositoryImpl(SpringDataProcessedOrderRepository springDataRepository, ProcessedOrderMapper mapper) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
    }

    @Override
    public ProcessedOrder save(ProcessedOrder processedOrder) {
        ProcessedOrderEntity entity = mapper.toEntity(processedOrder);
        ProcessedOrderEntity savedEntity = springDataRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<ProcessedOrder> findByOrderId(UUID orderId) {
        return springDataRepository.findByOrderId(orderId)
                .map(mapper::toDomain);
    }
}
