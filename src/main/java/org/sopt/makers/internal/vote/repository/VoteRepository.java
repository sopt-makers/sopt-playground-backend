package org.sopt.makers.internal.vote.repository;

import java.util.Optional;
import org.sopt.makers.internal.community.domain.CommunityPost;
import org.sopt.makers.internal.vote.domain.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByPost(CommunityPost post);
}
