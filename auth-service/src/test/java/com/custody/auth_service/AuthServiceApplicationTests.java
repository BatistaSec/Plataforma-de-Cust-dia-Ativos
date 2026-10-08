package com.custody.auth_service;

import com.custody.auth_service.domain.enums.Role;
import com.custody.auth_service.infrastructure.persistence.entities.UserEntity;
import com.custody.auth_service.infrastructure.persistence.repositories.SpringDataUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AuthServiceApplicationTests {

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private SpringDataUserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void setUp() {
		userRepository.deleteAll();
	}

	@Test
	void contextLoads() {
		assertTrue(postgres.isRunning());
	}

	@Test
	void shouldRegisterUserSuccessfully() {
		Map<String, String> request = Map.of(
				"email", "test@custody.com",
				"password", "secret123");

		ResponseEntity<Map> response = restTemplate.postForEntity("/api/v1/auth/register", request, Map.class);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertTrue(response.getBody().containsKey("token"));

		UserEntity user = userRepository.findByEmail("test@custody.com").orElse(null);
		assertNotNull(user);
		assertEquals(Role.USER, user.getRole());
	}

	@Test
	void shouldAuthenticateUserSuccessfully() {
		UserEntity user = new UserEntity(null, "login@custody.com", passwordEncoder.encode("password123"), Role.USER);
		userRepository.save(user);

		Map<String, String> request = Map.of(
				"email", "login@custody.com",
				"password", "password123");

		ResponseEntity<Map> response = restTemplate.postForEntity("/api/v1/auth/login", request, Map.class);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertTrue(response.getBody().containsKey("token"));
	}

	@Test
	void shouldFailAuthenticationWithWrongPassword() {
		UserEntity user = new UserEntity(null, "wrong@custody.com", passwordEncoder.encode("password123"), Role.USER);
		userRepository.save(user);

		Map<String, String> request = Map.of(
				"email", "wrong@custody.com",
				"password", "wrongpassword");

		ResponseEntity<Map> response = restTemplate.postForEntity("/api/v1/auth/login", request, Map.class);

		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}

}
