package com.ftabah.giftme.adapter.web;

import com.ftabah.giftme.application.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.security.core.Authentication;
import com.ftabah.giftme.application.AccountSecurityService;
import com.ftabah.giftme.application.port.TokenService;

/** Endpoints de autenticação, verificação, recuperação e logout. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AccountSecurityService accountSecurity;
    private final TokenService tokenService;

    public AuthController(AuthService authService, AccountSecurityService accountSecurity, TokenService tokenService) {
        this.authService = authService;
        this.accountSecurity = accountSecurity;
        this.tokenService = tokenService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthService.AuthResponse register(@Valid @RequestBody CredentialsRequest request) {
        return authService.register(request.email(), request.password());
    }

    @PostMapping("/login")
    public AuthService.AuthResponse login(@Valid @RequestBody CredentialsRequest request) {
        return authService.login(request.email(), request.password());
    }

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam String token) {
        accountSecurity.verify(token);
        return "E-mail verificado com sucesso.";
    }

    @PostMapping("/password/forgot")
    public void forgotPassword(@Valid @RequestBody EmailRequest request) {
        accountSecurity.requestPasswordReset(request.email().trim().toLowerCase());
    }

    @PostMapping("/password/reset")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        accountSecurity.resetPassword(request.token(), request.password());
    }

    @PostMapping("/logout")
    public void logout(@RequestHeader("Authorization") String authorization, Authentication authentication) {
        if (authentication != null && authorization.startsWith("Bearer ")) {
            tokenService.revoke(authorization.substring(7));
        }
    }

    public record CredentialsRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8) String password) { }

    public record EmailRequest(@NotBlank @Email String email) { }

    public record ResetPasswordRequest(@NotBlank String token, @NotBlank @Size(min = 8) String password) { }
}