package me.jas0n.web.auth;

import jakarta.validation.Valid;
import me.jas0n.domain.user.User;
import me.jas0n.service.auth.AuthService;
import me.jas0n.web.auth.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest req) {
        User u = auth.register(req);
        RegisterResponse body = new RegisterResponse(u.getId(), u.getEmail(), u.getName(), false);
        URI location = URI.create("/api/v1/users/" + u.getId());
        return ResponseEntity.created(location).body(body);
    }

    @PostMapping("/login")
    public AuthTokens login(@Valid @RequestBody LoginRequest req) {
        return auth.login(req);
    }

    @PostMapping("/refresh")
    public AuthTokens refresh(@Valid @RequestBody RefreshRequest req) {
        return auth.refresh(req.refreshToken());
    }
}