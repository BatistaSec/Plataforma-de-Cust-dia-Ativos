package com.custody.auth_service.application.usecases;

import com.custody.auth_service.application.dto.AuthOutput;
import com.custody.auth_service.application.dto.RegisterInput;
import com.custody.auth_service.domain.enums.Role;
import com.custody.auth_service.domain.models.User;
import com.custody.auth_service.domain.repositories.UserRepository;
import com.custody.auth_service.domain.valueobjects.Email;
import com.custody.auth_service.infrastructure.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public RegisterUserUseCase(UserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthOutput execute(RegisterInput input) {
        var email = new Email(input.getEmail());
        var user = new User(email, input.getPassword(), Role.USER);

        user.setPassword(passwordEncoder.encode(input.getPassword()));
        var savedUser = userRepository.save(user);

        var claims = Map.<String, Object>of("userId", savedUser.getId().toString());
        var jwtToken = jwtService.generateToken(claims, email.getValue());

        return new AuthOutput(jwtToken);
    }
}
