package org.sopt.makers.internal.wordchaingame.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.wordchaingame.domain.QWordChainGameRoom;
import org.sopt.makers.internal.wordchaingame.domain.QWordChainGameWinner;
import org.sopt.makers.internal.wordchaingame.domain.WordChainGameRoom;
import org.sopt.makers.internal.wordchaingame.domain.WordChainGameWinner;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WordChainGameQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<WordChainGameRoom> findAllLimitedGameRoom(
            Integer limit, Long cursor
    ) {
        val room = QWordChainGameRoom.wordChainGameRoom;
        return queryFactory.selectFrom(room)
                .where(ltGameRoomId(cursor))
                .limit(limit)
                .orderBy(room.id.desc())
                .groupBy(room.id)
                .fetch();
    }

    public List<WordChainGameRoom> findAllGameRoom() {
        val room = QWordChainGameRoom.wordChainGameRoom;
        return queryFactory.selectFrom(room)
                .groupBy(room.id)
                .orderBy(room.id.desc())
                .fetch();
    }

    public List<WordChainGameRoom> findGameRoomOrderByCreatedDesc() {
        val room = QWordChainGameRoom.wordChainGameRoom;
        return queryFactory.selectFrom(room)
                .orderBy(room.createdAt.desc())
                .fetch();
    }

    public List<WordChainGameWinner> findAllLimitedWinner(Integer limit, Integer cursor) {
        QWordChainGameWinner winner = QWordChainGameWinner.wordChainGameWinner;

        return queryFactory
                .select(winner)
                .from(winner)
                .offset(cursor)
                .limit(limit)
                .orderBy(winner.id.desc())
                .fetch();
    }

    private BooleanExpression ltGameRoomId(Long gameRoomId) {
        val room = QWordChainGameRoom.wordChainGameRoom;
        if(gameRoomId == null || gameRoomId == 0) return null;
        return room.id.lt(gameRoomId);
    }

    public long countByUserIdAndCreatedAtBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        QWordChainGameWinner winner = QWordChainGameWinner.wordChainGameWinner;
        QWordChainGameRoom gameRoom = QWordChainGameRoom.wordChainGameRoom;

        return Optional.ofNullable(queryFactory.select(winner.count())
            .from(winner)
            .innerJoin(gameRoom).on(winner.roomId.eq(gameRoom.id))
            .where(
                winner.userId.eq(userId)
                    .and(gameRoom.createdAt.between(startDate, endDate))
            )
            .fetchOne()
        ).orElse(0L);
    }
}
