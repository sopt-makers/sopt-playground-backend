package org.sopt.makers.internal.resolution.repository;

import org.sopt.makers.internal.resolution.domain.UserResolution;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserResolutionRepository extends JpaRepository<UserResolution, Long> {
}
