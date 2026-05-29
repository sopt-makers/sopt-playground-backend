package org.sopt.makers.internal.vote.repository;

import java.util.List;

import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.vote.domain.Vote;
import org.sopt.makers.internal.vote.domain.VoteOption;
import org.sopt.makers.internal.vote.domain.VoteSelection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VoteSelectionRepository extends JpaRepository<VoteSelection, Long> {

    boolean existsByVoteOptionInAndMember(List<VoteOption> voteOptions, Member member);

    List<VoteSelection> findByVoteOptionInAndMember(List<VoteOption> voteOptions, Member member);

    @Query("SELECT COUNT(DISTINCT vs.member.id) FROM VoteSelection vs WHERE vs.voteOption.vote = :vote")
    int countDistinctMembersByVote(@Param("vote") Vote vote);

    @Query("""
        SELECT voteOption.vote.id AS voteId,
               COUNT(DISTINCT voteSelection.member.id) AS participantCount
        FROM VoteSelection voteSelection
        JOIN voteSelection.voteOption voteOption
        WHERE voteOption.vote.id IN :voteIds
        GROUP BY voteOption.vote.id
    """)
    List<VoteParticipantCountProjection> countDistinctMembersByVoteIds(
        @Param("voteIds") List<Long> voteIds
    );

    @Query("""
        SELECT voteOption.vote.id AS voteId,
               voteOption.id AS optionId
        FROM VoteSelection voteSelection
        JOIN voteSelection.voteOption voteOption
        WHERE voteOption.vote.id IN :voteIds
          AND voteSelection.member.id = :memberId
    """)
    List<MemberVoteSelectionProjection> findSelectedOptionIdsByVoteIdsAndMemberId(
        @Param("voteIds") List<Long> voteIds,
        @Param("memberId") Long memberId
    );

    interface VoteParticipantCountProjection {
        Long getVoteId();
        Long getParticipantCount();
    }

    interface MemberVoteSelectionProjection {
        Long getVoteId();
        Long getOptionId();
    }
}
