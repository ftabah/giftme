package com.ftabah.giftme.application.port;

import java.util.UUID;

/** Porta para criação, validação e revogação de sessões autenticadas. */
public interface TokenService {

    String create(UUID userId);

    UUID parseUserId(String token);

    boolean isRevoked(String token);

    void revoke(String token);
}