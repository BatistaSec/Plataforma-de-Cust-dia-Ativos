package com.custody.custody_service.application.usecases;

import com.custody.custody_service.application.dto.CreateOrderInput;
import com.custody.custody_service.application.dto.OrderOutput;
import com.custody.custody_service.domain.enums.OrderStatus;
import com.custody.custody_service.domain.events.OrderEvent;
import com.custody.custody_service.domain.exceptions.ResourceNotFoundException;
import com.custody.custody_service.domain.models.Asset;
import com.custody.custody_service.domain.models.CustodyOrder;
import com.custody.custody_service.domain.models.Portfolio;
import com.custody.custody_service.domain.repositories.AssetRepository;
import com.custody.custody_service.domain.repositories.CustodyOrderRepository;
import com.custody.custody_service.domain.repositories.PortfolioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateOrderUseCase {

    private final CustodyOrderRepository orderRepository;
    private final PortfolioRepository portfolioRepository;
    private final AssetRepository assetRepository;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @Value("${kafka.topics.custody-orders:custody-orders-topic}")
    private String ordersTopic;

    public CreateOrderUseCase(CustodyOrderRepository orderRepository,
                               PortfolioRepository portfolioRepository,
                               AssetRepository assetRepository,
                               KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.portfolioRepository = portfolioRepository;
        this.assetRepository = assetRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public OrderOutput execute(CreateOrderInput input) {
        Portfolio portfolio = portfolioRepository.findById(input.getPortfolioId())
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio não encontrado"));

        Asset asset = assetRepository.findById(input.getAssetId())
                .orElseThrow(() -> new ResourceNotFoundException("Asset não encontrado"));

        // A validação de quantidade é feita no domínio (CustodyOrder)
        CustodyOrder order = new CustodyOrder(portfolio, asset, input.getType(), input.getQuantity(), OrderStatus.PENDING);
        CustodyOrder saved = orderRepository.save(order);

        // Publica evento no Kafka
        OrderEvent event = new OrderEvent(
                saved.getId(),
                portfolio.getId(),
                asset.getId(),
                saved.getType(),
                saved.getQuantity(),
                saved.getStatus()
        );
        kafkaTemplate.send(ordersTopic, saved.getId().toString(), event);

        return new OrderOutput(
                saved.getId(),
                portfolio.getId(),
                asset.getId(),
                saved.getType(),
                saved.getQuantity(),
                saved.getStatus(),
                saved.getCreatedAt()
        );
    }
}
