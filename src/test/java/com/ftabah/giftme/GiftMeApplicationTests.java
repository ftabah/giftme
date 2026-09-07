package com.ftabah.giftme;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = "giftme.storage.data-directory=target/test-data")
class GiftMeApplicationTests {

    @Autowired
    private CsvStorageProperties storageProperties;

    @Test
    void loadsApplicationContextWithConfiguredCsvDirectory() {
        assertThat(storageProperties.dataDirectory()).isEqualTo("target/test-data");
    }
}