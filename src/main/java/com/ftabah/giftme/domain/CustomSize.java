package com.ftabah.giftme.domain;

public record CustomSize(String name, String value) {

    public CustomSize {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Custom size name is required");
        }
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Custom size value is required");
        }
        name = name.trim();
        value = value.trim();
    }
}