package com.acme.saas.auth.api;

import com.acme.saas.auth.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    public record RegisterRequest(
            @Email @NotBlank String email,
            @NotBlank @Size(min = 8) String password,
            @NotBlank String fullName,
            @NotBlank String workspaceName) {}

    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}
    public record RefreshRequest(@NotBlank String refreshToken) {}

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthService.AuthTokens register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req.email(), req.password(), req.fullName(), req.workspaceName());
    }

    @PostMapping("/login")
    public AuthService.AuthTokens login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req.email(), req.password());
    }

    @PostMapping("/refresh")
    public AuthService.AuthTokens refresh(@Valid @RequestBody RefreshRequest req) {
        return authService.rotateRefreshToken(req.refreshToken());
    }
}
