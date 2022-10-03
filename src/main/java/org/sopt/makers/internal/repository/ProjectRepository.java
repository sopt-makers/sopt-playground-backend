package org.sopt.makers.internal.repository;

import org.sopt.makers.internal.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
