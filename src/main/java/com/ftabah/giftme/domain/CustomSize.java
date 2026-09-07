package com.ftabah.giftme.domain;

/** Armazena uma medida personalizada nomeada pelo usuário. */
public record CustomSize(String name, String value) {

    public CustomSize {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("O nome da medida personalizada é obrigatório");
        }
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("O valor da medida personalizada é obrigatório");
        }
        name = name.trim();
        value = value.trim();
    }
}