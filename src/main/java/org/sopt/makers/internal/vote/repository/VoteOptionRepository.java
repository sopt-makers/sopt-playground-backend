package org.sopt.makers.internal.vote.repository;

import org.sopt.makers.internal.vote.domain.VoteOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteOptionRepository extends JpaRepository<VoteOption, Long> {
}
