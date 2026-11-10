package org.sopt.makers.internal.wordchaingame.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.common.util.ConstraintViolationUtil;
import org.sopt.makers.internal.exception.BadRequestException;
import org.sopt.makers.internal.wordchaingame.domain.Word;
import org.sopt.makers.internal.wordchaingame.repository.WordRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class WordModifier {

    private static final String WORD_ROOM_WORD_UNIQUE_CONSTRAINT = "uk_word_room_id_word";

    private final WordRepository wordRepository;

    @Transactional
    public Word createWord(Long roomId, Long memberId, String word) {
        Word newWord = Word.builder()
            .roomId(roomId)
            .memberId(memberId)
            .word(word)
            .createdAt(LocalDateTime.now())
            .build();

        // 동시 요청이 서비스의 중복 검사를 함께 통과해 (room_id, word) 유니크 제약에 걸리면 400 으로 바꾼다
        try {
            return wordRepository.saveAndFlush(newWord);
        } catch (DataIntegrityViolationException exception) {
            if (ConstraintViolationUtil.isViolated(exception, WORD_ROOM_WORD_UNIQUE_CONSTRAINT)) {
                throw new BadRequestException("이미 누군가 사용한 단어예요.");
            }
            throw exception;
        }
    }
}
