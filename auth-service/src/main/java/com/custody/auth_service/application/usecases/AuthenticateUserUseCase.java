package com.custody.auth_service.application.usecases;

import com.custody.auth_service.application.dto.AuthInput;
import com.custody.auth_service.application.dto.AuthOutput;
import com.custody.auth_service.domain.exceptions.UserNotFoundException;
import com.custody.auth_service.domain.repositories.UserRepository;
import com.custody.auth_service.infrastructure.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthenticateUserUseCase {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticateUserUseCase(UserRepository userRepository,
                                    JwtService jwtService,
                                    AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthOutput execute(AuthInput input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(input.getEmail(), input.getPassword())
        );

        var user = userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado: " + input.getEmail()));

        var claims = Map.<String, Object>of("userId", user.getId().toString());
        var jwtToken = jwtService.generateToken(claims, user.getEmail().getValue());

        return new AuthOutput(jwtToken);
    }
}
