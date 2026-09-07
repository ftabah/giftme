package com.ftabah.giftme.application;

import com.ftabah.giftme.adapter.storage.csv.AccountActionTokenStore;
import com.ftabah.giftme.adapter.web.EmailProperties;
import com.ftabah.giftme.application.port.AccountRepository;
import com.ftabah.giftme.application.port.EmailSender;
import com.ftabah.giftme.application.port.PasswordHasher;
import com.ftabah.giftme.domain.Account;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@Service
public class AccountSecurityService {

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
        UUID userId = tokens.consume(token, "VERIFY", Instant.now())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired verification token"));
        tokens.markVerified(userId);
    }

    public void requestPasswordReset(String email) {
        accounts.findByEmail(email).ifPresent(account -> {
            String token = tokens.issue(account.id(), "RESET", expiresAt());
            emailSender.sendPasswordReset(account.email(), properties.baseUrl() + "/reset-password?token=" + token);
        });
    }

    public void resetPassword(String token, String rawPassword) {
        if (rawPassword == null || rawPassword.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must contain at least 8 characters");
        }
        UUID userId = tokens.consume(token, "RESET", Instant.now())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired reset token"));
        Account account = accounts.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account not found"));
        accounts.save(new Account(account.id(), account.email(), passwordHasher.hash(rawPassword)));
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
}