package com.ftabah.giftme;

import com.ftabah.giftme.adapter.storage.csv.CsvFileStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.nio.file.Paths;

/** Ponto de entrada da aplicação GiftMe e configuração dos adaptadores Spring. */
@SpringBootApplication
@EnableConfigurationProperties(CsvStorageProperties.class)
public class GiftMeApplication {

    @Bean
    CsvFileStore csvFileStore(CsvStorageProperties properties) {
        return new CsvFileStore(Paths.get(properties.dataDirectory()));
    }

    public static void main(String[] args) {
        SpringApplication.run(GiftMeApplication.class, args);
    }
}