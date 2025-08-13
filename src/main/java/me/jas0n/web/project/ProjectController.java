package me.jas0n.web.project;

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
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody CreateProjectRequest req) {
        UUID uid = CurrentUser.idOrThrow();
        ProjectResponse body = projects.create(req, uid);
        URI location = URI.create("/api/v1/projects/" + body.id());
        return ResponseEntity.created(location).body(body);
    }

    @GetMapping
    public Page<ProjectResponse> list(
            @RequestParam(name = "status", required = false) List<ProjectStatus> statusList,
            org.springframework.data.domain.Pageable pageable
    ) {
        java.util.Optional<java.util.List<ProjectStatus>> statuses =
                java.util.Optional.ofNullable((statusList == null || statusList.isEmpty()) ? null : statusList);
        return projects.list(statuses, pageable);
    }

    @GetMapping("/{id}")
    public ProjectResponse get(@PathVariable UUID id) {
        return projects.get(id);
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<Void> archive(@PathVariable UUID id) {
        UUID uid = CurrentUser.idOrThrow();
        projects.archive(id, uid);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<Void> restore(@PathVariable UUID id) {
        UUID uid = CurrentUser.idOrThrow();
        projects.restore(id, uid);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
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

}
