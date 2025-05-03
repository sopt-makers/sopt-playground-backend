package org.sopt.makers.internal.project.repository;

import org.sopt.makers.internal.project.domain.ProjectLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectLinkRepository extends JpaRepository<ProjectLink, Long> {
    List<ProjectLink> findAllByProjectId(Long projectId);

    void deleteAllByProjectId(Long projectId);
}
