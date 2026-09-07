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

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
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

    public record CredentialsRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8) String password) { }
}