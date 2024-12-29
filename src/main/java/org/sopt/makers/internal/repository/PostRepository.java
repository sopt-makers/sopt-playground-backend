package org.sopt.makers.internal.repository;

import org.sopt.makers.internal.community.domain.CommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.util.Optional;

public interface PostRepository extends JpaRepository<CommunityPost, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT cp FROM CommunityPost cp WHERE cp.id = :id")
    Optional<CommunityPost> findByIdForIncreaseViewCount(@Param("id") Long id);
}
