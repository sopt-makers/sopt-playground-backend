package org.sopt.makers.internal.repository;

import org.sopt.makers.internal.domain.soulmate.Soulmate;
import org.sopt.makers.internal.domain.soulmate.SoulmateState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SoulmateRepository extends JpaRepository<Soulmate, Long> {
    Optional<Soulmate> findByMateIdAndId(Long userId, Long soulmateId);
    List<Soulmate> findAllByState(SoulmateState state);
    List<Soulmate> findAllByStateOrderByStateModifiedAtDesc(SoulmateState state);
    Optional<Soulmate> findByMateIdAndStateIsNot (Long userId, SoulmateState state);
    Optional<Soulmate> findByMateIdAndStateIsNotOrMateIdAndStateIsNot (
            Long mateId, SoulmateState stateOne,
            Long secondaryMateId, SoulmateState stateTwo
    );

    List<Soulmate> findAllByMateIdOrderByStartAtDesc(Long userId);
}
