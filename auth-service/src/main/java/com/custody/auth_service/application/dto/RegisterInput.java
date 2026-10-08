package com.custody.auth_service.application.dto;

public class RegisterInput {

    private final String email;
    private final String password;

    public RegisterInput(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() { return email; }
    public String getPassword() { return password; }
}
