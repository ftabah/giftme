package com.ftabah.giftme.adapter.storage.csv;

import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Armazena tokens de conta com hash, expiração e uso único. */
@Repository
public class AccountActionTokenStore {

    private static final String TOKEN_FILE = "account_action_tokens.csv";
    private static final String VERIFIED_FILE = "verified_accounts.csv";
    private final CsvFileStore store;
    private final SecureRandom random = new SecureRandom();

    public AccountActionTokenStore(CsvFileStore store) {
        this.store = store;
    }

    public synchronized String issue(UUID userId, String type, Instant expiresAt) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        try {
            List<List<String>> rows = new ArrayList<>(store.read(TOKEN_FILE));
            rows.add(List.of(digest(raw), userId.toString(), type, expiresAt.toString(), "false"));
            store.replace(TOKEN_FILE, rows);
            return raw;
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível emitir o token da conta", exception);
        }
    }

    public synchronized Optional<UUID> consume(String raw, String type, Instant now) {
        try {
            List<List<String>> rows = new ArrayList<>(store.read(TOKEN_FILE));
            Optional<UUID> result = Optional.empty();
            List<List<String>> updated = new ArrayList<>();
            for (List<String> row : rows) {
                boolean match = row.size() == 5 && row.get(0).equals(digest(raw)) && row.get(2).equals(type)
                        && !Boolean.parseBoolean(row.get(4)) && Instant.parse(row.get(3)).isAfter(now);
                if (match) {
                    result = Optional.of(UUID.fromString(row.get(1)));
                    updated.add(List.of(row.get(0), row.get(1), row.get(2), row.get(3), "true"));
                } else {
                    updated.add(row);
                }
            }
            if (result.isPresent()) {
                store.replace(TOKEN_FILE, updated);
            }
            return result;
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("Não foi possível consumir o token da conta", exception);
        }
    }

    public synchronized void markVerified(UUID userId) {
        try {
            List<List<String>> rows = new ArrayList<>(store.read(VERIFIED_FILE));
            if (rows.stream().noneMatch(row -> row.size() == 1 && row.get(0).equals(userId.toString()))) {
                rows.add(List.of(userId.toString()));
                store.replace(VERIFIED_FILE, rows);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível marcar a conta como verificada", exception);
        }
    }

    public boolean isVerified(UUID userId) {
        try {
            return store.read(VERIFIED_FILE).stream()
                    .anyMatch(row -> row.size() == 1 && row.get(0).equals(userId.toString()));
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível ler o estado de verificação", exception);
        }
    }

    private static String digest(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Não foi possível gerar o hash do token da conta", exception);
        }
    }
}