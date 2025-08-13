package me.jas0n.service.project;

import me.jas0n.common.error.BusinessException;
import me.jas0n.common.text.Slugs;
import me.jas0n.domain.project.*;
import me.jas0n.repository.project.ProjectMemberRepository;
import me.jas0n.repository.project.ProjectRepository;
import me.jas0n.web.project.dto.CreateProjectRequest;
import me.jas0n.web.project.dto.ProjectResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projects;
    private final ProjectMemberRepository members;

    public ProjectService(ProjectRepository projects, ProjectMemberRepository members) {
        this.projects = projects;
        this.members = members;
    }

    @Transactional
    public ProjectResponse create(CreateProjectRequest req, UUID currentUserId) {
        if (currentUserId == null) {
            throw new BusinessException("UNAUTHORIZED", "未登录或凭证无效", HttpStatus.UNAUTHORIZED);
        }

        // 基本唯一性：未删除项目中 title 不重复（可按需放宽）
        if (projects.existsByTitleAndDeletedAtIsNull(req.title())) {
            throw new BusinessException("DUPLICATE_TITLE", "该项目名称已存在", HttpStatus.CONFLICT);
        }

        String base = Slugs.slugify(req.title());
        String slug = base;
        int i = 2;
        while (projects.existsBySlug(slug)) {
            slug = base + "-" + i;
            i++;
        }

        Project p = new Project();
        p.setProgramId(req.programId());
        p.setTitle(req.title().trim());
        p.setStatus(req.status() == null ? ProjectStatus.ONGOING : req.status());
        p.setDescription(req.description());
        p.setCreatedBy(currentUserId);
        p.setSlug(slug);

        Project saved = projects.save(p);

        // 插入成员：创建者 = OWNER
        ProjectMember owner = new ProjectMember(new ProjectMemberId(saved.getId(), currentUserId), ProjectRole.OWNER);
        members.save(owner);

        return new ProjectResponse(
                saved.getId(),
                saved.getProgramId(),
                saved.getTitle(),
                saved.getStatus(),
                saved.getDescription(),
                saved.getSlug(),
                saved.getCreatedBy(),
                saved.getCreatedAt(),
                saved.getLastActivityAt()
        );
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponse> list(Optional<List<ProjectStatus>> statuses, Pageable pageable) {
        Page<Project> page = statuses.isPresent() && !statuses.get().isEmpty()
                ? projects.findByDeletedAtIsNullAndStatusIn(statuses.get(), pageable)
                : projects.findByDeletedAtIsNull(pageable);

        return page.map(p -> new ProjectResponse(
                p.getId(), p.getProgramId(), p.getTitle(), p.getStatus(),
                p.getDescription(), p.getSlug(), p.getCreatedBy(),
                p.getCreatedAt(), p.getLastActivityAt()
        ));
    }

    @Transactional(readOnly = true)
    public ProjectResponse get(UUID id) {
        Project p = projects.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BusinessException("PROJECT_NOT_FOUND", "项目不存在", HttpStatus.NOT_FOUND));
        return new ProjectResponse(
                p.getId(), p.getProgramId(), p.getTitle(), p.getStatus(),
                p.getDescription(), p.getSlug(), p.getCreatedBy(),
                p.getCreatedAt(), p.getLastActivityAt()
        );
    }

    @Transactional
    public void archive(UUID id, UUID currentUserId) {
        Project p = projects.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BusinessException("PROJECT_NOT_FOUND", "项目不存在", HttpStatus.NOT_FOUND));

        ProjectRole highest = getHighestRole(id, currentUserId);
        if ((highest != ProjectRole.OWNER && highest != ProjectRole.MANAGER)) {
            throw new BusinessException("NO_PERMISSION", "无权限执行此操作", HttpStatus.FORBIDDEN);
        }

        if (p.getStatus() != ProjectStatus.ARCHIVED) {
            p.setStatus(ProjectStatus.ARCHIVED);
            p.setLastActivityAt(Instant.now());
        }
    }

    @Transactional
    public void restore(UUID id, UUID currentUserId) {
        Project p = projects.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BusinessException("PROJECT_NOT_FOUND", "项目不存在", HttpStatus.NOT_FOUND));
        if (p.getStatus() == ProjectStatus.ARCHIVED) {
            p.setStatus(ProjectStatus.ONGOING);
            p.setLastActivityAt(Instant.now());
        } else {
            throw new BusinessException("INVALID_STATUS_TRANSITION", "仅已归档可恢复", HttpStatus.BAD_REQUEST);
        }
    }

    public ProjectRole getHighestRole(UUID projectId, UUID userId) {
        if (userId == null) {
            throw new BusinessException("UNAUTHORIZED", "未登录或凭证无效", HttpStatus.UNAUTHORIZED);
        }

        List<ProjectMember> roles = members.findByIdProjectIdAndIdUserId(projectId, userId);
        if (roles.isEmpty()) {
            return null; // 说明不是成员
        }

        // 返回权限最高的那个（枚举 ordinal 小的等级最高）
        return roles.stream()
                .map(ProjectMember::getRole)
                .min(Comparator.comparingInt(ProjectRole::ordinal))
                .orElse(null);
    }
}