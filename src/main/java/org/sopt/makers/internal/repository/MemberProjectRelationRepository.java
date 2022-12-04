package org.sopt.makers.internal.repository;

import org.sopt.makers.internal.domain.MemberProjectRelation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberProjectRelationRepository extends JpaRepository<MemberProjectRelation, Long> {
    List<MemberProjectRelation> findAllByProjectId(Long projectId);

    void deleteAllByProjectId(Long projectId);
}
