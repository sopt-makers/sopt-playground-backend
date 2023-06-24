package org.sopt.makers.internal.repository;

import org.sopt.makers.internal.domain.soulmate.Soulmate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SoulmateRepository extends JpaRepository<Soulmate, Long> {
}
