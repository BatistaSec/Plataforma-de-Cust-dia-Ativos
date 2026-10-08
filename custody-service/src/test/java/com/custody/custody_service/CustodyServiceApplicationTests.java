package com.custody.custody_service;

import com.custody.custody_service.domain.enums.OrderType;
import com.custody.custody_service.infrastructure.persistence.entities.AssetEntity;
import com.custody.custody_service.infrastructure.persistence.entities.PortfolioEntity;
import com.custody.custody_service.infrastructure.persistence.repositories.SpringDataAssetRepository;
import com.custody.custody_service.infrastructure.persistence.repositories.SpringDataCustodyOrderRepository;
import com.custody.custody_service.infrastructure.persistence.repositories.SpringDataPortfolioRepository;
import com.custody.custody_service.presentation.response.AssetResponse;
import com.custody.custody_service.presentation.response.CustodyOrderResponse;
import com.custody.custody_service.presentation.response.PortfolioResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class CustodyServiceApplicationTests {

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

	@Container
	@ServiceConnection
	static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

	@Container
	@ServiceConnection(name = "redis")
	static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private SpringDataPortfolioRepository portfolioRepository;

	@Autowired
	private SpringDataAssetRepository assetRepository;

	@Autowired
	private SpringDataCustodyOrderRepository custodyOrderRepository;

	@BeforeEach
	void setUp() {
		custodyOrderRepository.deleteAll();
		portfolioRepository.deleteAll();
		assetRepository.deleteAll();
	}

	@Test
	void contextLoads() {
		assertTrue(postgres.isRunning());
		assertTrue(kafka.isRunning());
		assertTrue(redis.isRunning());
	}

	@Test
	void shouldCreatePortfolioSuccessfullyaddUserId() {
		UUID userId = UUID.randomUUID();
		
		ResponseEntity<PortfolioResponse> response = restTemplate.postForEntity(
				"/api/v1/custody/portfolios?userId=" + userId, 
				null, 
				PortfolioResponse.class
		);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(userId, response.getBody().getUserId());
	}

	@Test
	void shouldGetPortfolioSuccessddUserId() {
		UUID userId = UUID.randomUUID();
		PortfolioEntity portfolio = new PortfolioEntity();
		portfolio.setUserId(userId);
		portfolioRepository.save(portfolio);

		ResponseEntity<PortfolioResponse> response = restTemplate.getForEntity(
				"/api/v1/custody/portfolios/user/" + userId,
				PortfolioResponse.class
		);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(userId, response.getBody().getUserId());
	}

	@Test
	void shouldCreateAssetSuccessfully() {
		ResponseEntity<AssetResponse> response = restTemplate.postForEntity(
				"/api/v1/custody/assets?ticker=AAPL&name=Apple Inc", 
				null, 
				AssetResponse.class
		);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("AAPL", response.getBody().getTicker());
	}

	@Test
	void shouldGetAllAssetsSuccessfully() {
		AssetEntity asset1 = new AssetEntity();
		asset1.setTicker("ITUB4");
		asset1.setName("Itaú Unibanco");
		assetRepository.save(asset1);

		AssetEntity asset2 = new AssetEntity();
		asset2.setTicker("BBDC4");
		asset2.setName("Bradesco");
		assetRepository.save(asset2);

		ResponseEntity<AssetResponse[]> response = restTemplate.getForEntity(
				"/api/v1/custody/assets",
				AssetResponse[].class
		);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertTrue(response.getBody().length >= 2);
	}

	@Test
	void shouldCreateOrderSuccessfully() {
		UUID userId = UUID.randomUUID();
		PortfolioEntity portfolio = new PortfolioEntity();
		portfolio.setUserId(userId);
		portfolio = portfolioRepository.save(portfolio);

		AssetEntity asset = new AssetEntity();
		asset.setTicker("PETR4");
		asset.setName("Petrobras");
		asset = assetRepository.save(asset);

		Map<String, Object> request = Map.of(
				"portfolioId", portfolio.getId(),
				"assetId", asset.getId(),
				"type", "BUY",
				"quantity", 10.5
		);

		ResponseEntity<CustodyOrderResponse> response = restTemplate.postForEntity(
				"/api/v1/custody/orders", 
				request, 
				CustodyOrderResponse.class
		);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(OrderType.BUY, response.getBody().getType());
		assertEquals(new BigDecimal("10.5"), response.getBody().getQuantity());
	}
}
