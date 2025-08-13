package me.jas0n.repository.project;

import me.jas0n.domain.project.ProjectMember;
import me.jas0n.domain.project.ProjectMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {
    boolean existsByIdProjectIdAndIdUserId(UUID projectId, UUID userId);

    List<ProjectMember> findByIdProjectIdAndIdUserId(UUID projectId, UUID userId);
}
