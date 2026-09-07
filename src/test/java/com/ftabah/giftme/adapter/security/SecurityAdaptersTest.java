package com.ftabah.giftme.adapter.security;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityAdaptersTest {

    private final JwtTokenService tokenService = new JwtTokenService(
            new JwtProperties("test-secret-with-at-least-32-characters-long", 3600));

    @Test
    void hashesPasswordsAndMatchesOnlyTheOriginalPassword() {
        BcryptPasswordHasher hasher = new BcryptPasswordHasher();
        String hash = hasher.hash("correct-password");

        assertThat(hash).doesNotContain("correct-password");
        assertThat(hasher.matches("correct-password", hash)).isTrue();
        assertThat(hasher.matches("wrong-password", hash)).isFalse();
    }

    @Test
    void createsTokenWithUserIdAndRejectsTampering() {
        UUID userId = UUID.randomUUID();
        String token = tokenService.create(userId);

        assertThat(tokenService.parseUserId(token)).isEqualTo(userId);
        assertThatThrownBy(() -> tokenService.parseUserId(token + "x"))
                .isInstanceOf(RuntimeException.class);
    }
}