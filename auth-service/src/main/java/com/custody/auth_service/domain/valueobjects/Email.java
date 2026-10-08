package com.custody.auth_service.domain.valueobjects;

import com.custody.auth_service.domain.exceptions.BusinessException;

import java.util.Objects;
import java.util.regex.Pattern;

public class Email {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );

    private final String value;

    public Email(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException("O email não pode ser vazio");
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new BusinessException("O email deve ser válido");
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return Objects.equals(value, email.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
