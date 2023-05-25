package org.sopt.makers.internal.repository;

import org.sopt.makers.internal.domain.Sopticle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SopticleRepository extends JpaRepository<Sopticle, Long> {
}
