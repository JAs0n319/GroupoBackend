package me.jas0n.repository.project;

import me.jas0n.domain.project.ProjectMember;
import me.jas0n.domain.project.ProjectMemberId;
import me.jas0n.domain.project.ProjectStatus;
import me.jas0n.repository.project.row.ProjectWithRoleRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {
    boolean existsByIdProjectIdAndIdUserId(UUID projectId, UUID userId);

    List<ProjectMember> findByIdProjectIdAndIdUserId(UUID projectId, UUID userId);

    @Query(
            value = """
                    select p as project, m.role as role
                    from Project p
                    join ProjectMember m on m.id.projectId = p.id
                    where m.id.userId = :uid
                      and p.deletedAt is null
                      and (:q is null or lower(p.title) like lower(concat('%', :q, '%')))
                      and (:statuses is null or p.status in :statuses)
                    """,
            countQuery = """
                    select count(p.id)
                    from Project p
                    join ProjectMember m on m.id.projectId = p.id
                    where m.id.userId = :uid
                      and p.deletedAt is null
                      and (:q is null or lower(p.title) like lower(concat('%', :q, '%')))
                      and (:statuses is null or p.status in :statuses)
                    """
    )
    Page<ProjectWithRoleRow> findMyProjects(
            @Param("uid") UUID userId,
            @Param("q") String q,
            @Param("statuses") List<ProjectStatus> statuses,
            Pageable pageable
    );
}
