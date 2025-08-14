package me.jas0n.web.project;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import me.jas0n.domain.project.ProjectStatus;
import me.jas0n.security.CurrentUser;
import me.jas0n.service.project.ProjectService;
import me.jas0n.web.project.dto.CreateProjectRequest;
import me.jas0n.web.project.dto.ProjectResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectService projects;

    public ProjectController(ProjectService projects) {
        this.projects = projects;
    }

    @PostMapping
    @Operation(summary = "创建项目")
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody CreateProjectRequest req) {
        UUID uid = CurrentUser.idOrThrow();
        ProjectResponse body = projects.create(req, uid);
        URI location = URI.create("/api/v1/projects/" + body.id());
        return ResponseEntity.created(location).body(body);
    }

    @GetMapping
    @Operation(summary = "获取项目列表")
    public Page<ProjectResponse> list(
            @RequestParam(name = "status", required = false) List<ProjectStatus> statusList,
            org.springframework.data.domain.Pageable pageable
    ) {
        java.util.Optional<java.util.List<ProjectStatus>> statuses =
                java.util.Optional.ofNullable((statusList == null || statusList.isEmpty()) ? null : statusList);
        return projects.list(statuses, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取项目详情")
    public ProjectResponse get(@PathVariable UUID id) {
        return projects.get(id);
    }

    @PostMapping("/{id}/archive")
    @Operation(summary = "归档项目")
    public ResponseEntity<Void> archive(@PathVariable UUID id) {
        UUID uid = CurrentUser.idOrThrow();
        projects.archive(id, uid);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/pause")
    @Operation(summary = "暂停项目")
    public ResponseEntity<Void> pause(@PathVariable UUID id) {
        UUID uid = CurrentUser.idOrThrow();
        projects.pause(id, uid);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "恢复项目")
    public ResponseEntity<Void> restore(@PathVariable UUID id) {
        UUID uid = CurrentUser.idOrThrow();
        projects.restore(id, uid);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    @Operation(summary = "获取个人项目页面")
    public Page<ProjectResponse> myProjects(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "status", required = false) List<ProjectStatus> statuses,
            Pageable pageable
    ) {
        UUID uid = CurrentUser.idOrThrow();
        return projects.listMyProjects(
                uid,
                Optional.ofNullable(q),
                Optional.ofNullable(statuses),
                pageable
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除项目")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        UUID uid = CurrentUser.idOrThrow();
        projects.delete(id, uid);
        return ResponseEntity.noContent().build(); // 204
    }

}
