package com.ftabah.giftme;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "giftme.storage")
public record CsvStorageProperties(String dataDirectory) {

    public CsvStorageProperties {
        if (dataDirectory == null || dataDirectory.isBlank()) {
            dataDirectory = "data";
        }
    }
}