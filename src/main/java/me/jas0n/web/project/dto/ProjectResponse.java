package me.jas0n.web.project.dto;

import me.jas0n.domain.project.ProjectStatus;

import java.time.Instant;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        UUID programId,
        String title,
        ProjectStatus status,
        String description,
        String slug,
        UUID createdBy,
        Instant createdAt,
        Instant lastActivityAt
) {
}
