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
    public String getRandomStartWord() {
        val random = new Random();
        int number = random.nextInt(soptWord.size());
        return soptWord.get(number);
    }

    private final List<String> soptWord = List.of("메이커스", "고솝트", "플레이그라운드", "버디버디", "개발", "피그마", "솝트마인드", "마라탕", "음악", "디자이너", "애자일", "퇴사", "햇살티미단", "종무식", "서울", "제주", "감자", "휴지", "물고기", "책상", "햄버거", "선물", "미소", "맛집", "가방", "의자", "열정", "운동", "성장", "일기", "추억", "이야기");

    @Transactional
    public Word saveWord (Member member, WordChainGameGenerateRequest request) {
        return wordRepository.save(Word.builder().roomId(request.roomId()).memberId(member.getId())
                .word(request.word()).createdAt(LocalDateTime.now()).build());
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
