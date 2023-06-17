package org.sopt.makers.internal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.sopt.makers.internal.domain.*;
import org.sopt.makers.internal.dto.wordChainGame.WordChainGameGenerateRequest;
import org.sopt.makers.internal.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class WordChainGameService {
    @Value("${dictionary.key}")
    private String dictionaryKey;
    private final WordRepository wordRepository;
    private final WordChainGameRepository wordChainGameRepository;
    private final WordChainGameQueryRepository wordChainGameQueryRepository;

    @Transactional
    public Word createWord(Member member, WordChainGameGenerateRequest request) {
        val word = request.word();
        val hasDuplicateWord = (wordRepository.findByWordAndRoomId(word, request.roomId()).size() > 1);
        if(hasDuplicateWord) throw new WordChainGameHasWrongInputException("이미 누군가 사용한 단어예요.");
        val isWordInDictionary = checkWordExistInDictionary(word);
        if(!isWordInDictionary) throw new WordChainGameHasWrongInputException("표준국어대사전에 존재하지 않는 단어예요.");
        val isRecentWordExist = wordRepository.findAllByRoomIdOrderByCreatedAt(request.roomId());
        if(isRecentWordExist.size() > 0) {
            val lastWord = isRecentWordExist.get(0).getWord();
            val isChainingWordSame = request.word().charAt(0) == lastWord.charAt(lastWord.length() - 1);
            if(!isChainingWordSame) throw new WordChainGameHasWrongInputException("끝말을 잇는 단어가 아니에요.");
        }
        return wordRepository.save(Word.builder().roomId(request.roomId()).memberId(member.getId())
                .word(word).createdAt(LocalDateTime.now()).build());
    }

    @Transactional
    public WordChainGameRoom createWordGameRoom(Member member) {
        return wordChainGameRepository.save(WordChainGameRoom.builder()
                        .createdAt(LocalDateTime.now())
                        .startWord(getRandomStartWord())
                        .createdUserId(member.getId())
                .build());
    }

    @Transactional
    public List<WordChainGameRoom> getAllRoom(Integer limit, Integer cursor) {
        if(limit != null) {
            return wordChainGameQueryRepository.findAllLimitedGameRoom(limit, cursor);
        }
        else {
            return wordChainGameQueryRepository.findAllGameRoom();
        }
    }
}
