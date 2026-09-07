package com.ftabah.giftme.application.port;

import com.ftabah.giftme.domain.Account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Porta de persistência e consulta de contas. */
public interface AccountRepository {

    Optional<Account> findById(UUID id);

    Optional<Account> findByEmail(String normalizedEmail);

    List<Account> findByNameQuery(String query);

    Account save(Account account);
}