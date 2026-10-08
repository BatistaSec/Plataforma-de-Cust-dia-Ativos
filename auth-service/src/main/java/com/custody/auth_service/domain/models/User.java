package com.custody.auth_service.domain.models;

import com.custody.auth_service.domain.enums.Role;
import com.custody.auth_service.domain.exceptions.InvalidPasswordException;
import com.custody.auth_service.domain.valueobjects.Email;

import java.util.UUID;


public class User {

    private UUID id;
    private Email email;
    private String password;
    private Role role;

    public User(Email email, String password, Role role) {
        validatePassword(password);
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public User(UUID id, Email email, String password, Role role) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new InvalidPasswordException("A senha deve ter pelo menos 8 caracteres");
        }
    }

    // Getters
    public UUID getId() { return id; }
    public Email getEmail() { return email; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }

    // Setters controlados
    public void setId(UUID id) { this.id = id; }
    public void setPassword(String encodedPassword) { this.password = encodedPassword; }
}
