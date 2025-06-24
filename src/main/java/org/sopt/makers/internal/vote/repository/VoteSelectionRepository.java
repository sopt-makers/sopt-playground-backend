package org.sopt.makers.internal.vote.repository;

import java.util.List;

import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.vote.domain.VoteOption;
import org.sopt.makers.internal.vote.domain.VoteSelection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteSelectionRepository extends JpaRepository<VoteSelection, Long> {
    boolean existsByVoteOptionInAndMember(List<VoteOption> voteOptions, Member member);
    List<VoteSelection> findByVoteOptionInAndMember(List<VoteOption> voteOptions, Member member);
}
