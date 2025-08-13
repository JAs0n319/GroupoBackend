package me.jas0n.repository.project;

import me.jas0n.domain.project.Project;
import me.jas0n.domain.project.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    boolean existsBySlug(String slug);

    boolean existsByTitleAndDeletedAtIsNull(String title);

    Optional<Project> findByIdAndDeletedAtIsNull(UUID id);

    Page<Project> findByDeletedAtIsNullAndStatusIn(List<ProjectStatus> statuses, Pageable pageable);

    Page<Project> findByDeletedAtIsNull(Pageable pageable);
}
