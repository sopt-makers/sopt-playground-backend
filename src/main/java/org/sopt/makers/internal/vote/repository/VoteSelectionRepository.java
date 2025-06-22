package org.sopt.makers.internal.vote.repository;

import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.vote.domain.Vote;
import org.sopt.makers.internal.vote.domain.VoteSelection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteSelectionRepository extends JpaRepository<VoteSelection, Long> {
    boolean existsByVoteAndMember(Vote vote, Member member);
}
