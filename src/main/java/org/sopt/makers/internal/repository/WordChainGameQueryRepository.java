package org.sopt.makers.internal.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.domain.*;
import org.sopt.makers.internal.dto.wordChainGame.QWinnerDao;
import org.sopt.makers.internal.dto.wordChainGame.WinnerDao;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class WordChainGameQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<WordChainGameRoom> findAllLimitedGameRoom(
            Integer limit, Long cursor
    ) {
        val room = QWordChainGameRoom.wordChainGameRoom;
        return queryFactory.selectFrom(room)
                .offset(cursor)
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

    public List<WinnerDao> findAllLimitedWinner(
            Integer limit, Integer cursor
    ) {
        val member = QMember.member;
        val winner = QWordChainGameWinner.wordChainGameWinner;

        return queryFactory.select(new QWinnerDao(
                        winner.id, winner.roomId, member.id, member.name, member.profileImage
                )).from(winner)
                .innerJoin(member).on(winner.userId.eq(member.id))
                .offset(cursor)
                .limit(limit)
                .orderBy(winner.id.desc())
                .fetch();
    }

    public List<WinnerDao> findAllWinner() {
        val member = QMember.member;
        val winner = QWordChainGameWinner.wordChainGameWinner;
        return queryFactory.select(new QWinnerDao(
                        winner.id, winner.roomId, member.id, member.name, member.profileImage
                )).from(winner)
                .innerJoin(member).on(winner.userId.eq(member.id))
                .orderBy(winner.id.desc())
                .fetch();
    }
}