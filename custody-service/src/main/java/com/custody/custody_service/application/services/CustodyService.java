package com.custody.custody_service.application.services;

import com.custody.custody_service.domain.events.OrderEvent;
import com.custody.custody_service.domain.models.Asset;
import com.custody.custody_service.domain.models.CustodyOrder;
import com.custody.custody_service.domain.models.OrderStatus;
import com.custody.custody_service.domain.models.OrderType;
import com.custody.custody_service.domain.models.Portfolio;
import com.custody.custody_service.domain.repositories.AssetRepository;
import com.custody.custody_service.domain.repositories.CustodyOrderRepository;
import com.custody.custody_service.domain.repositories.PortfolioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class CustodyService {

    private final CustodyOrderRepository orderRepository;
    private final PortfolioRepository portfolioRepository;
    private final AssetRepository assetRepository;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @Value("${kafka.topics.custody-orders:custody-orders-topic}")
    private String ordersTopic;

    public CustodyService(CustodyOrderRepository orderRepository,
                          PortfolioRepository portfolioRepository,
                          AssetRepository assetRepository,
                          KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.portfolioRepository = portfolioRepository;
        this.assetRepository = assetRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public Portfolio createPortfolio(UUID userId) {
        if (portfolioRepository.findByUserId(userId).isPresent()) {
            throw new IllegalArgumentException("Portfolio already exists for this user");
        }
        Portfolio portfolio = new Portfolio(userId);
        return portfolioRepository.save(portfolio);
    }

    @Transactional(readOnly = true)
    public Portfolio getPortfolioByUserId(UUID userId) {
        return portfolioRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found for user: " + userId));
    }

    @Cacheable(value = "assets")
    @Transactional(readOnly = true)
    public List<Asset> getAllAssets() {
        return assetRepository.findAll();
    }

    @Transactional
    public Asset createAsset(String ticker, String name) {
        Asset asset = new Asset(ticker, name);
        return assetRepository.save(asset);
    }

    @Transactional
    public CustodyOrder createOrder(UUID portfolioId, UUID assetId, OrderType type, BigDecimal quantity) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found"));
                
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found"));

        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        CustodyOrder order = new CustodyOrder(portfolio, asset, type, quantity, OrderStatus.PENDING);
        order = orderRepository.save(order);

        // Publish event to Kafka
        OrderEvent event = new OrderEvent(
                order.getId(),
                portfolio.getId(),
                asset.getId(),
                order.getType(),
                order.getQuantity(),
                order.getStatus()
        );
        kafkaTemplate.send(ordersTopic, order.getId().toString(), event);

        return order;
    }
}
