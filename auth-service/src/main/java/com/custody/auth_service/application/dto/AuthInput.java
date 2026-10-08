package com.custody.auth_service.application.dto;

public class AuthInput {

    private final String email;
    private final String password;

    public AuthInput(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() { return email; }
    public String getPassword() { return password; }
}
