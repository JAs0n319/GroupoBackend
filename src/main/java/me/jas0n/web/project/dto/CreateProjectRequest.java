package me.jas0n.web.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import me.jas0n.domain.project.ProjectStatus;

import java.util.List;
import java.util.UUID;

public record CreateProjectRequest(
        UUID programId,
        @NotBlank @Size(min = 2, max = 100) String title,
        ProjectStatus status,                 // 可空则默认 Ongoing
        @Size(max = 2000) String description,
        List<String> labels,                  // 先占位；本迭代不入库也没问题
        List<UUID> nodeIds                    // 同上
) {
}
