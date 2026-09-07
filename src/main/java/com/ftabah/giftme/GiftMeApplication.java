package com.ftabah.giftme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(CsvStorageProperties.class)
public class GiftMeApplication {

    public static void main(String[] args) {
        SpringApplication.run(GiftMeApplication.class, args);
    }
}