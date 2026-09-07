package com.ftabah.giftme.domain;

import java.util.UUID;

/** Representa uma conta local sem expor a senha em texto puro. */
public record Account(UUID id, String email, String passwordHash) {

    public Account {
        if (id == null) {
            throw new IllegalArgumentException("O identificador da conta é obrigatório");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("O e-mail é obrigatório");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("O hash da senha é obrigatório");
        }
        email = email.trim().toLowerCase();
    }
}