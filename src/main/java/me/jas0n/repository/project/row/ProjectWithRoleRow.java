package me.jas0n.repository.project.row;

import me.jas0n.domain.project.Project;
import me.jas0n.domain.project.ProjectRole;

public interface ProjectWithRoleRow {
    Project getProject();

    ProjectRole getRole();
}
