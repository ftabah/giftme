package com.ftabah.giftme.application;

import com.ftabah.giftme.adapter.storage.csv.AccountActionTokenStore;
import com.ftabah.giftme.adapter.web.EmailProperties;
import com.ftabah.giftme.application.port.AccountRepository;
import com.ftabah.giftme.application.port.EmailSender;
import com.ftabah.giftme.application.port.PasswordHasher;
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

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class AccountSecurityServiceTest {

    @Mock
    private AccountActionTokenStore tokens;
    @Mock
    private AccountRepository accounts;
    @Mock
    private PasswordHasher passwordHasher;
    @Mock
    private EmailSender emailSender;

    private AccountSecurityService accountSecurity;

    @BeforeEach
    void setUp() {
        accountSecurity = new AccountSecurityService(tokens, accounts, passwordHasher, emailSender,
                new EmailProperties("https://giftme.example", 30, false));
    }

    @Test
    void logsWhenResetEmailDoesNotMatchAnAccount(CapturedOutput output) {
        when(accounts.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        accountSecurity.requestPasswordReset("missing@example.com");

        assertTrue(output.toString().contains("Recuperação de senha solicitada para conta não cadastrada (m***@example.com)"));
        verify(emailSender, never()).sendPasswordReset(anyString(), anyString());
    }

    @Test
    void logsWhenResetEmailIsSent(CapturedOutput output) {
        Account account = new Account(UUID.randomUUID(), "test@example.com", "hash");
        when(accounts.findByEmail(account.email())).thenReturn(Optional.of(account));
        when(tokens.issue(eq(account.id()), eq("RESET"), any(Instant.class))).thenReturn("reset-token");

        accountSecurity.requestPasswordReset(account.email());

        assertTrue(output.toString().contains("E-mail de recuperação enviado (t***@example.com)"));
        verify(emailSender).sendPasswordReset(account.email(),
                "https://giftme.example/reset-password?token=reset-token");
    }

    @Test
    void logsAndPropagatesMailFailure(CapturedOutput output) {
        Account account = new Account(UUID.randomUUID(), "test@example.com", "hash");
        when(accounts.findByEmail(account.email())).thenReturn(Optional.of(account));
        when(tokens.issue(eq(account.id()), eq("RESET"), any(Instant.class))).thenReturn("reset-token");
        doThrow(new IllegalStateException("SMTP unavailable"))
                .when(emailSender).sendPasswordReset(eq(account.email()), anyString());

        assertThrows(IllegalStateException.class, () -> accountSecurity.requestPasswordReset(account.email()));

        assertTrue(output.toString().contains("Falha ao enviar e-mail de recuperação (t***@example.com, IllegalStateException)"));
    }

    @Test
    void logsSuccessfulPasswordResetWithoutLoggingTheToken(CapturedOutput output) {
        Account account = new Account(UUID.randomUUID(), "test@example.com", "old-hash");
        when(tokens.consume(eq("reset-token"), eq("RESET"), any(Instant.class)))
                .thenReturn(Optional.of(account.id()));
        when(accounts.findById(account.id())).thenReturn(Optional.of(account));
        when(passwordHasher.hash("newpass123")).thenReturn("new-hash");

        accountSecurity.resetPassword("reset-token", "newpass123");

        verify(accounts).save(new Account(account.id(), account.email(), "new-hash"));
        assertTrue(output.toString().contains("Senha redefinida (t***@example.com)"));
        assertFalse(output.toString().contains("reset-token"));
    }

    @Test
    void logsWhenResetTokenIsInvalid(CapturedOutput output) {
        when(tokens.consume(eq("invalid-token"), eq("RESET"), any(Instant.class))).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> accountSecurity.resetPassword("invalid-token", "newpass123"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(output.toString().contains("Redefinição de senha recusada: token inválido ou expirado"));
        assertFalse(output.toString().contains("invalid-token"));
    }
}