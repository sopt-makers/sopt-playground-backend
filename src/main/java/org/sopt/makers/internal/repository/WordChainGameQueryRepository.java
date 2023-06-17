package org.sopt.makers.internal.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.domain.QWord;
import org.sopt.makers.internal.domain.QWordChainGameRoom;
import org.sopt.makers.internal.domain.WordChainGameRoom;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class WordChainGameQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<WordChainGameRoom> findAllLimitedGameRoom(
            Integer limit, Integer cursor
    ) {
        val room = QWordChainGameRoom.wordChainGameRoom;
        val wordList = QWord.word1;
        return queryFactory.selectFrom(room)
                .innerJoin(room.wordList, wordList)
                .offset(cursor)
                .limit(limit)
                .orderBy(room.id.desc())
                .groupBy(room.id)
                .fetch();
    }

    public List<WordChainGameRoom> findAllGameRoom() {
        val room = QWordChainGameRoom.wordChainGameRoom;
        val wordList = QWord.word1;
        return queryFactory.selectFrom(room)
                .innerJoin(room.wordList, wordList)
                .groupBy(room.id)
                .orderBy(room.id.desc())
                .fetch();
    }
}