package me.jas0n.domain.project;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "project_members")
public class ProjectMember {

    @EmbeddedId
    private ProjectMemberId id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ProjectRole role;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt;

    public ProjectMember() {
    }

    public ProjectMember(ProjectMemberId id, ProjectRole role) {
        this.id = id;
        this.role = role;
        this.addedAt = Instant.now();
    }

    public ProjectMemberId getId() {
        return id;
    }

    public ProjectRole getRole() {
        return role;
    }

    public void setRole(ProjectRole role) {
        this.role = role;
    }

    public Instant getAddedAt() {
        return addedAt;
    }
}
