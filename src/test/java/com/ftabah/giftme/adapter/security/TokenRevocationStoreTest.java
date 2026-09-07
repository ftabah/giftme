package com.ftabah.giftme.adapter.security;

import com.ftabah.giftme.adapter.storage.csv.CsvFileStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class TokenRevocationStoreTest {

    @TempDir
    Path dataDirectory;

    @Test
    void revokedTokenRemainsRevokedAfterStoreReload() {
        TokenRevocationStore first = new TokenRevocationStore(new CsvFileStore(dataDirectory));
        first.revoke("token-to-revoke");

        TokenRevocationStore reloaded = new TokenRevocationStore(new CsvFileStore(dataDirectory));

        assertThat(reloaded.isRevoked("token-to-revoke")).isTrue();
        assertThat(reloaded.isRevoked("other-token")).isFalse();
    }
}