package com.custody.auth_service.presentation.controllers;

import com.custody.auth_service.application.dto.AuthInput;
import com.custody.auth_service.application.dto.RegisterInput;
import com.custody.auth_service.application.dto.AuthOutput;
import com.custody.auth_service.application.usecases.AuthenticateUserUseCase;
import com.custody.auth_service.application.usecases.RegisterUserUseCase;
import com.custody.auth_service.presentation.request.AuthRequest;
import com.custody.auth_service.presentation.response.AuthResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;

    public AuthController(RegisterUserUseCase registerUserUseCase,
                          AuthenticateUserUseCase authenticateUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.authenticateUserUseCase = authenticateUserUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest request) {
        var input = new RegisterInput(request.getEmail(), request.getPassword());
        AuthOutput output = registerUserUseCase.execute(input);
        return ResponseEntity.ok(new AuthResponse(output.getToken()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticate(@Valid @RequestBody AuthRequest request) {
        var input = new AuthInput(request.getEmail(), request.getPassword());
        AuthOutput output = authenticateUserUseCase.execute(input);
        return ResponseEntity.ok(new AuthResponse(output.getToken()));
    }
}
