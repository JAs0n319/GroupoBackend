package me.jas0n.web.project;

import me.jas0n.domain.project.Project;
import me.jas0n.web.project.dto.ProjectResponse;

public final class ProjectMapper {
    private ProjectMapper() {
    }

    public static ProjectResponse toResponse(Project p) {
        return new ProjectResponse(
                p.getId(),
                p.getProgramId(),
                p.getTitle(),
                p.getStatus(),
                p.getDescription(),
                p.getSlug(),
                p.getCreatedBy(),
                p.getCreatedAt(),
                p.getLastActivityAt()
        );
    }
}
