package org.sopt.makers.internal.repository;

import org.sopt.makers.internal.domain.Link;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LinkRepository extends JpaRepository<Link, Long> {
    List<Link> findAllByProjectId(Long projectId);
}
