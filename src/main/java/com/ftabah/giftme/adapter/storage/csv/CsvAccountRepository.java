package com.ftabah.giftme.adapter.storage.csv;

import com.ftabah.giftme.application.port.AccountRepository;
import com.ftabah.giftme.domain.Account;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CsvAccountRepository implements AccountRepository {

    private static final String FILE = "accounts.csv";
    private final CsvFileStore store;

    public CsvAccountRepository(CsvFileStore store) {
        this.store = store;
    }

    @Override
    public Optional<Account> findById(UUID id) {
        return readAll().stream().filter(account -> account.id().equals(id)).findFirst();
    }

    @Override
    public Optional<Account> findByEmail(String normalizedEmail) {
        return readAll().stream().filter(account -> account.email().equals(normalizedEmail)).findFirst();
    }

    @Override
    public List<Account> findByNameQuery(String query) {
        String normalized = query.trim().toLowerCase();
        return readAll().stream().filter(account -> account.email().contains(normalized)).toList();
    }

    @Override
    public Account save(Account account) {
        List<Account> accounts = readAll().stream()
                .filter(existing -> !existing.id().equals(account.id()))
                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
        accounts.add(account);
        write(accounts.stream().map(this::toRow).toList());
        return account;
    }

    private List<Account> readAll() {
        try {
            return store.read(FILE).stream().map(this::fromRow).toList();
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("Could not read accounts", exception);
        }
    }

    private void write(List<List<String>> rows) {
        try {
            store.replace(FILE, rows);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not write accounts", exception);
        }
    }

    private Account fromRow(List<String> row) {
        if (row.size() != 3) {
            throw new IllegalStateException("Invalid account record");
        }
        return new Account(UUID.fromString(row.get(0)), row.get(1), row.get(2));
    }

    private List<String> toRow(Account account) {
        return List.of(account.id().toString(), account.email(), account.passwordHash());
    }
}