package org.sopt.makers.internal.repository;

import org.sopt.makers.internal.domain.Word;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface WordRepository extends JpaRepository<Word, Long> {
    Boolean existsByWordAndRoomId(String word, Long roomId);
    Word findFirstByRoomIdOrderByCreatedAtDesc(Long roomId);
    List<Word> findAllByMemberIdAndCreatedAtBetween(Long memberId, LocalDateTime start, LocalDateTime end);
}
