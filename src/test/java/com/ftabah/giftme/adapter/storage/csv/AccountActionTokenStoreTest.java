package com.ftabah.giftme.adapter.storage.csv;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AccountActionTokenStoreTest {

    @TempDir
    Path dataDirectory;

    @Test
    void tokensAreSingleUseAndExpiredTokensAreRejected() {
        AccountActionTokenStore tokens = new AccountActionTokenStore(new CsvFileStore(dataDirectory));
        UUID userId = UUID.randomUUID();
        String usable = tokens.issue(userId, "VERIFY", Instant.now().plusSeconds(60));
        String expired = tokens.issue(userId, "RESET", Instant.now().minusSeconds(1));

        assertThat(tokens.consume(usable, "VERIFY", Instant.now())).contains(userId);
        assertThat(tokens.consume(usable, "VERIFY", Instant.now())).isEmpty();
        assertThat(tokens.consume(expired, "RESET", Instant.now())).isEmpty();
    }
}