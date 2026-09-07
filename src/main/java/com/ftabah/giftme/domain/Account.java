package com.ftabah.giftme.domain;

import java.util.UUID;

public record Account(UUID id, String email, String passwordHash) {

    public Account {
        if (id == null) {
            throw new IllegalArgumentException("Account id is required");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("Password hash is required");
        }
        email = email.trim().toLowerCase();
    }
}