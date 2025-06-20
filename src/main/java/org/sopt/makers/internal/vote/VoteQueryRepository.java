package org.sopt.makers.internal.vote;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.vote.domain.QVote;
import org.sopt.makers.internal.vote.domain.QVoteOption;
import org.sopt.makers.internal.vote.domain.QVoteSelection;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class VoteQueryRepository {

    private final JPAQueryFactory queryFactory;

    private final QVote vote = QVote.vote;
    private final QVoteOption voteOption = QVoteOption.voteOption;
    private final QVoteSelection voteSelection = QVoteSelection.voteSelection;
}
