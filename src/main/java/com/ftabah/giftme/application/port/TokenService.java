package com.ftabah.giftme.application.port;

import java.util.UUID;

public interface TokenService {

    String create(UUID userId);

    UUID parseUserId(String token);
}