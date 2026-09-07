package com.ftabah.giftme.adapter.security;

import com.ftabah.giftme.adapter.storage.csv.CsvFileStore;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/** Persiste hashes de tokens JWT revogados para impedir reutilização. */
@Repository
public class TokenRevocationStore {

    private static final String FILE = "revoked_tokens.csv";
    private final CsvFileStore store;

    public TokenRevocationStore(CsvFileStore store) {
        this.store = store;
    }

    public synchronized boolean isRevoked(String token) {
        String digest = digest(token);
        try {
            return store.read(FILE).stream().anyMatch(row -> row.size() == 1 && row.get(0).equals(digest));
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível ler os tokens revogados", exception);
        }
    }

    public synchronized void revoke(String token) {
        try {
            var rows = new java.util.ArrayList<>(store.read(FILE));
            rows.add(java.util.List.of(digest(token)));
            store.replace(FILE, rows);
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível revogar o token", exception);
        }
    }

    private static String digest(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Não foi possível gerar o hash do token", exception);
        }
    }
}