package me.jas0n.web.auth.dto;

import java.util.UUID;

public record RegisterResponse(
        UUID id,
        String email,
        String name,
        boolean requiresEmailVerification
) {
}
