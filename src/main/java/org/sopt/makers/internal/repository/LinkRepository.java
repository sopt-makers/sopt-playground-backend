package org.sopt.makers.internal.repository;

import org.sopt.makers.internal.domain.ProjectLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LinkRepository extends JpaRepository<ProjectLink, Long> {
    List<ProjectLink> findAllByProjectId(Long projectId);
}
