package com.ftabah.giftme.adapter.web;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "giftme.email")
public record EmailProperties(String baseUrl, long tokenExpirationMinutes, boolean requireVerification) {

    public EmailProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://localhost:8080";
        }
        if (tokenExpirationMinutes <= 0) {
            tokenExpirationMinutes = 30;
        }
    }
}