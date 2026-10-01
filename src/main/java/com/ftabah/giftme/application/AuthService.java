package com.ftabah.giftme.application;

import com.ftabah.giftme.application.port.AccountRepository;
import com.ftabah.giftme.application.port.PasswordHasher;
import com.ftabah.giftme.application.port.TokenService;
import com.ftabah.giftme.domain.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/** Coordena cadastro, login e emissão de sessões autenticadas. */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AccountRepository accounts;
    private final PasswordHasher passwordHasher;
    private final TokenService tokenService;
    private final AccountSecurityService accountSecurity;

    public AuthService(AccountRepository accounts, PasswordHasher passwordHasher, TokenService tokenService,
                       AccountSecurityService accountSecurity) {
        this.accounts = accounts;
        this.passwordHasher = passwordHasher;
        this.tokenService = tokenService;
        this.accountSecurity = accountSecurity;
    }

    public RegistrationResponse register(String email, String rawPassword) {
        validatePassword(rawPassword);
        String normalizedEmail = normalizeEmail(email);
        if (accounts.findByEmail(normalizedEmail).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O e-mail já está em uso");
        }
        Account account = accounts.save(new Account(UUID.randomUUID(), normalizedEmail, passwordHasher.hash(rawPassword)));
        accountSecurity.sendVerification(account);
        return new RegistrationResponse(account.id(), accountSecurity.requiresVerification());
    }

    public AuthResponse login(String email, String rawPassword) {
        String normalizedEmail = normalizeEmail(email);
        Account account = accounts.findByEmail(normalizedEmail).orElse(null);
        if (account == null) {
            log.warn("Login recusado: conta não cadastrada ({})", maskEmail(normalizedEmail));
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }
        if (!passwordHasher.matches(rawPassword, account.passwordHash())) {
            log.warn("Login recusado: senha inválida ({})", maskEmail(normalizedEmail));
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }
        if (accountSecurity.requiresVerification() && !accountSecurity.isVerified(account.id())) {
            log.warn("Login recusado: e-mail não verificado ({})", maskEmail(normalizedEmail));
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "O e-mail deve ser verificado antes do login");
        }
        return new AuthResponse(account.id(), tokenService.create(account.id()));
    }

    private static void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A senha deve conter pelo menos 8 caracteres");
        }
    }

    private static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O e-mail é obrigatório");
        }
        return email.trim().toLowerCase();
    }

    private static String maskEmail(String email) {
        int separator = email.indexOf('@');
        if (separator <= 0) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(separator);
    }

    public record RegistrationResponse(UUID userId, boolean emailVerificationRequired) { }

    public record AuthResponse(UUID userId, String token) { }
}