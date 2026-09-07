package com.ftabah.giftme.application;

import com.ftabah.giftme.application.port.AccountRepository;
import com.ftabah.giftme.application.port.PasswordHasher;
import com.ftabah.giftme.application.port.TokenService;
import com.ftabah.giftme.domain.Account;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class AuthService {

    private final AccountRepository accounts;
    private final PasswordHasher passwordHasher;
    private final TokenService tokenService;

    public AuthService(AccountRepository accounts, PasswordHasher passwordHasher, TokenService tokenService) {
        this.accounts = accounts;
        this.passwordHasher = passwordHasher;
        this.tokenService = tokenService;
    }

    public AuthResponse register(String email, String rawPassword) {
        validatePassword(rawPassword);
        String normalizedEmail = normalizeEmail(email);
        if (accounts.findByEmail(normalizedEmail).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }
        Account account = accounts.save(new Account(UUID.randomUUID(), normalizedEmail, passwordHasher.hash(rawPassword)));
        return new AuthResponse(account.id(), tokenService.create(account.id()));
    }

    public AuthResponse login(String email, String rawPassword) {
        Account account = accounts.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwordHasher.matches(rawPassword, account.passwordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return new AuthResponse(account.id(), tokenService.create(account.id()));
    }

    private static void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must contain at least 8 characters");
        }
    }

    private static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        return email.trim().toLowerCase();
    }

    public record AuthResponse(UUID userId, String token) { }
}