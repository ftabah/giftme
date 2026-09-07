package com.ftabah.giftme.adapter.storage.csv;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CsvFileStoreIntegrationTest {

    @TempDir
    Path dataDirectory;

    @Test
    void roundTripsEscapedFieldsEmptyValuesAndBase64() throws Exception {
        CsvFileStore store = new CsvFileStore(dataDirectory);
        String encoded = Base64.getEncoder().encodeToString("photo".getBytes(StandardCharsets.UTF_8));
        List<List<String>> rows = List.of(List.of("Ana, Silva", "\"M\"", "", encoded));

        store.replace("profiles.csv", rows);

        assertThat(store.read("profiles.csv")).containsExactly(rows.get(0));
    }

    @Test
    void missingFileIsReadAsEmptyAndReplacementLeavesNoTemporaryFile() throws Exception {
        CsvFileStore store = new CsvFileStore(dataDirectory);

        assertThat(store.read("missing.csv")).isEmpty();
        store.replace("accounts.csv", List.of(List.of("id", "email")));

        assertThat(Files.exists(dataDirectory.resolve("accounts.csv"))).isTrue();
        assertThat(Files.list(dataDirectory).filter(path -> path.getFileName().toString().endsWith(".tmp")))
                .isEmpty();
    }
}