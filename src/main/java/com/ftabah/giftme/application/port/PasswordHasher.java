package com.ftabah.giftme.application.port;

/** Porta para hash e verificação segura de senhas. */
public interface PasswordHasher {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String passwordHash);
}