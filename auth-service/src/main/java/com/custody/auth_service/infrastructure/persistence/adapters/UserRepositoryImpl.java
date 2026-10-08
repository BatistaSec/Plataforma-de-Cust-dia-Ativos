package com.custody.auth_service.infrastructure.persistence.adapters;

import com.custody.auth_service.domain.models.User;
import com.custody.auth_service.domain.repositories.UserRepository;
import com.custody.auth_service.infrastructure.mappers.UserMapper;
import com.custody.auth_service.infrastructure.persistence.entities.UserEntity;
import com.custody.auth_service.infrastructure.persistence.repositories.SpringDataUserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;


@Component
public class UserRepositoryImpl implements UserRepository {

    private final SpringDataUserRepository springDataRepository;

    public UserRepositoryImpl(SpringDataUserRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public User save(User user) {
        UserEntity entity = UserMapper.toEntity(user);
        UserEntity saved = springDataRepository.save(entity);
        return UserMapper.toDomain(saved);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springDataRepository.findByEmail(email)
                .map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return springDataRepository.findById(id)
                .map(UserMapper::toDomain);
    }
}
