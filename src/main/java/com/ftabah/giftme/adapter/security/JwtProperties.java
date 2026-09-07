package com.ftabah.giftme.adapter.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "giftme.security")
public record JwtProperties(String secret, long expirationSeconds) {

    public JwtProperties {
        if (secret == null || secret.length() < 32) {
            secret = "giftme-development-secret-change-before-production-2026";
        }
        if (expirationSeconds <= 0) {
            expirationSeconds = 3600;
        }
    }
}