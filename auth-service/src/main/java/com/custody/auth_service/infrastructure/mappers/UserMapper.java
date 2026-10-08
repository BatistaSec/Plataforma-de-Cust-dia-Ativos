package com.custody.auth_service.infrastructure.mappers;

import com.custody.auth_service.domain.enums.Role;
import com.custody.auth_service.domain.models.User;
import com.custody.auth_service.domain.valueobjects.Email;
import com.custody.auth_service.infrastructure.persistence.entities.UserEntity;


public class UserMapper {

    private UserMapper() {}

    public static UserEntity toEntity(User user) {
        return new UserEntity(
                user.getId(),
                user.getEmail().getValue(),
                user.getPassword(),
                user.getRole()
        );
    }

    public static User toDomain(UserEntity entity) {
        return new User(
                entity.getId(),
                new Email(entity.getEmail()),
                entity.getPassword(),
                entity.getRole()
        );
    }
}
