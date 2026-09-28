package com.custody.auth_service.application.services;

import com.custody.auth_service.domain.models.Role;
import com.custody.auth_service.domain.models.User;
import com.custody.auth_service.domain.repositories.UserRepository;
import com.custody.auth_service.infrastructure.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public Map<String, String> register(String email, String password) {
        var user = new User(email, passwordEncoder.encode(password), Role.USER);
        repository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return Map.of("token", jwtToken);
    }

    public Map<String, String> authenticate(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        var user = repository.findByEmail(email).orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        return Map.of("token", jwtToken);
    }
}
