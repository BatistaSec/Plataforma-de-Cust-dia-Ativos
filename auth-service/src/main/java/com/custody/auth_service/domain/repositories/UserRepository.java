package com.custody.auth_service.domain.repositories;

import com.custody.auth_service.domain.models.User;

import java.util.Optional;
import java.util.UUID;


public interface UserRepository {

    User save(User user);

    Optional<User> findByEmail(String email);

    Optional<User> findById(UUID id);
}
