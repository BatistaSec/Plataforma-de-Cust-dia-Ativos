package com.custody.custody_service;

import com.custody.custody_service.domain.models.Asset;
import com.custody.custody_service.domain.models.CustodyOrder;
import com.custody.custody_service.domain.models.OrderType;
import com.custody.custody_service.domain.models.Portfolio;
import com.custody.custody_service.domain.repositories.AssetRepository;
import com.custody.custody_service.domain.repositories.CustodyOrderRepository;
import com.custody.custody_service.domain.repositories.PortfolioRepository;
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
	private PortfolioRepository portfolioRepository;

	@Autowired
	private AssetRepository assetRepository;

	@Autowired
	private CustodyOrderRepository custodyOrderRepository;

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
		
		ResponseEntity<Portfolio> response = restTemplate.postForEntity(
				"/api/v1/custody/portfolios?userId=" + userId, 
				null, 
				Portfolio.class
		);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(userId, response.getBody().getUserId());
	}



	@Test
	void shouldGetPortfolioSuccessddUserId() {
		UUID userId = UUID.randomUUID();
		Portfolio portfolio = portfolioRepository.save(new Portfolio(userId));

		ResponseEntity<Portfolio> response = restTemplate.getForEntity(
				"/api/v1/custody/portfolios/user/" + userId,
				Portfolio.class
		);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(userId, response.getBody().getUserId());
	}



	@Test
	void shouldCreateAssetSuccessfully() {
		ResponseEntity<Asset> response = restTemplate.postForEntity(
				"/api/v1/custody/assets?ticker=AAPL&name=Apple Inc", 
				null, 
				Asset.class
		);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("AAPL", response.getBody().getTicker());
	}

	@Test
	void shouldGetAllAssetsSuccessfully() {
		assetRepository.save(new Asset("ITUB4", "Itaú Unibanco"));
		assetRepository.save(new Asset("BBDC4", "Bradesco"));

		ResponseEntity<Asset[]> response = restTemplate.getForEntity(
				"/api/v1/custody/assets",
				Asset[].class
		);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertTrue(response.getBody().length >= 2);
	}

	@Test
	void shouldCreateOrderSuccessfully() {
		UUID userId = UUID.randomUUID();
		Portfolio portfolio = portfolioRepository.save(new Portfolio(userId));

		Asset asset = assetRepository.save(new Asset("PETR4", "Petrobras"));

		Map<String, Object> request = Map.of(
				"portfolioId", portfolio.getId(),
				"assetId", asset.getId(),
				"type", "BUY",
				"quantity", 10.5
		);

		
		ResponseEntity<CustodyOrder> response = restTemplate.postForEntity(
				"/api/v1/custody/orders", 
				request, 
				CustodyOrder.class
		);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(OrderType.BUY, response.getBody().getType());
		assertEquals(new BigDecimal("10.5"), response.getBody().getQuantity());
	}




}
