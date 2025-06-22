package org.sopt.makers.internal.vote.repository;

import org.sopt.makers.internal.vote.domain.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote, Long> {
}
