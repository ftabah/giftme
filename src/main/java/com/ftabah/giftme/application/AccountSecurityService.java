package com.ftabah.giftme.application;

import com.ftabah.giftme.adapter.storage.csv.AccountActionTokenStore;
import com.ftabah.giftme.adapter.web.EmailProperties;
import com.ftabah.giftme.application.port.AccountRepository;
import com.ftabah.giftme.application.port.EmailSender;
import com.ftabah.giftme.application.port.PasswordHasher;
import com.ftabah.giftme.domain.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/** Gerencia verificação de e-mail, recuperação de senha e validade da conta. */
@Service
public class AccountSecurityService {

    private static final Logger log = LoggerFactory.getLogger(AccountSecurityService.class);

    private final AccountActionTokenStore tokens;
    private final AccountRepository accounts;
    private final PasswordHasher passwordHasher;
    private final EmailSender emailSender;
    private final EmailProperties properties;

    public AccountSecurityService(AccountActionTokenStore tokens, AccountRepository accounts,
                                  PasswordHasher passwordHasher, EmailSender emailSender,
                                  EmailProperties properties) {
        this.tokens = tokens;
        this.accounts = accounts;
        this.passwordHasher = passwordHasher;
        this.emailSender = emailSender;
        this.properties = properties;
    }

    public void sendVerification(Account account) {
        String token = tokens.issue(account.id(), "VERIFY", expiresAt());
        emailSender.sendVerification(account.email(), properties.baseUrl() + "/api/auth/verify-email?token=" + token);
    }

    public void verify(String token) {
        Optional<UUID> userId = tokens.consume(token, "VERIFY", Instant.now());
        if (userId.isEmpty()) {
            log.warn("Verificação de e-mail recusada: token inválido ou expirado");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token de verificação inválido ou expirado");
        }
        tokens.markVerified(userId.get());
        log.info("E-mail verificado para conta {}", userId.get());
    }

    public void requestPasswordReset(String email) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        String maskedEmail = maskEmail(normalizedEmail);
        log.info("Solicitação de recuperação de senha recebida ({})", maskedEmail);
        Optional<Account> accountMatch = accounts.findByEmail(normalizedEmail);
        if (accountMatch.isEmpty()) {
            log.warn("Recuperação de senha solicitada para conta não cadastrada ({})", maskedEmail);
            return;
        }

        Account account = accountMatch.get();
        String token = tokens.issue(account.id(), "RESET", expiresAt());
        try {
            emailSender.sendPasswordReset(account.email(), properties.baseUrl() + "/reset-password?token=" + token);
            log.info("E-mail de recuperação enviado ({})", maskedEmail);
        } catch (RuntimeException exception) {
            log.error("Falha ao enviar e-mail de recuperação ({}, {})", maskedEmail,
                    exception.getClass().getSimpleName(), exception);
            throw exception;
        }
    }

    public void resetPassword(String token, String rawPassword) {
        if (rawPassword == null || rawPassword.length() < 8) {
            log.warn("Redefinição de senha recusada: nova senha abaixo do tamanho mínimo");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A senha deve conter pelo menos 8 caracteres");
        }
        Optional<UUID> userId = tokens.consume(token, "RESET", Instant.now());
        if (userId.isEmpty()) {
            log.warn("Redefinição de senha recusada: token inválido ou expirado");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token de recuperação inválido ou expirado");
        }
        Optional<Account> accountMatch = accounts.findById(userId.get());
        if (accountMatch.isEmpty()) {
            log.warn("Redefinição de senha recusada: conta não encontrada");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Conta não encontrada");
        }
        Account account = accountMatch.get();
        try {
            accounts.save(new Account(account.id(), account.email(), passwordHasher.hash(rawPassword)));
            log.info("Senha redefinida ({})", maskEmail(account.email()));
        } catch (RuntimeException exception) {
            log.error("Falha ao salvar nova senha ({}, {})", maskEmail(account.email()),
                    exception.getClass().getSimpleName(), exception);
            throw exception;
        }
    }

    public boolean isVerified(UUID userId) {
        return tokens.isVerified(userId);
    }

    public boolean requiresVerification() {
        return properties.requireVerification();
    }

    private Instant expiresAt() {
        return Instant.now().plusSeconds(properties.tokenExpirationMinutes() * 60);
    }

    private static String maskEmail(String email) {
        int separator = email.indexOf('@');
        if (separator <= 0) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(separator);
    }
}