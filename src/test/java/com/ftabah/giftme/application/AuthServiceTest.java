package com.ftabah.giftme.application;

import com.ftabah.giftme.application.port.AccountRepository;
import com.ftabah.giftme.application.port.PasswordHasher;
import com.ftabah.giftme.application.port.TokenService;
import com.ftabah.giftme.domain.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class AuthServiceTest {

    @Mock
    private AccountRepository accounts;
    @Mock
    private PasswordHasher passwordHasher;
    @Mock
    private TokenService tokenService;
    @Mock
    private AccountSecurityService accountSecurity;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(accounts, passwordHasher, tokenService, accountSecurity);
    }

    @Test
    void logsWhenAccountDoesNotExist(CapturedOutput output) {
        when(accounts.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authService.login("missing@example.com", "secret123"));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertTrue(output.toString().contains("Login recusado: conta não cadastrada (m***@example.com)"));
        assertFalse(output.toString().contains("missing@example.com"));
    }

    @Test
    void logsWhenPasswordIsInvalid(CapturedOutput output) {
        Account account = account();
        when(accounts.findByEmail(account.email())).thenReturn(Optional.of(account));
        when(passwordHasher.matches("wrong-pass", account.passwordHash())).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authService.login(account.email(), "wrong-pass"));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertTrue(output.toString().contains("Login recusado: senha inválida (t***@example.com)"));
    }

    @Test
    void logsWhenEmailIsNotVerified(CapturedOutput output) {
        Account account = account();
        when(accounts.findByEmail(account.email())).thenReturn(Optional.of(account));
        when(passwordHasher.matches("secret123", account.passwordHash())).thenReturn(true);
        when(accountSecurity.requiresVerification()).thenReturn(true);
        when(accountSecurity.isVerified(account.id())).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authService.login(account.email(), "secret123"));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertTrue(output.toString().contains("Login recusado: e-mail não verificado (t***@example.com)"));
    }

    private static Account account() {
        return new Account(UUID.randomUUID(), "test@example.com", "hashed-password");
    }
}