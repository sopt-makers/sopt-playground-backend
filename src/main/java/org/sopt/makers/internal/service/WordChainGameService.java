package org.sopt.makers.internal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.Word;
import org.sopt.makers.internal.domain.WordChainGameRoom;
import org.sopt.makers.internal.dto.wordChainGame.WordChainGameGenerateRequest;
import org.sopt.makers.internal.exception.WordChainGameHasWrongInputException;
import org.sopt.makers.internal.repository.WordChainGameQueryRepository;
import org.sopt.makers.internal.repository.WordChainGameRepository;
import org.sopt.makers.internal.repository.WordRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Random;

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

    private boolean checkIsNotChainingWord(String lastWord, String nextWord) {
        return nextWord.charAt(0) != lastWord.charAt(lastWord.length() - 1);
    }

    private boolean checkWordExistInDictionary(String search){

        StringBuffer result = new StringBuffer();
        try {
            String apiUrl = "http://opendict.korean.go.kr/api/search?key=" + dictionaryKey + "&req_type=json&q=" + search;
            URL url = new URL(apiUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
            String returnLine;
            while ((returnLine = bufferedReader.readLine()) != null) {
                result.append(returnLine);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONParser parser = new JSONParser();
        JSONObject object = null;
        try {
            object = (JSONObject) parser.parse(String.valueOf(result));
        }catch (ParseException e){
            e.printStackTrace();
        }
        JSONObject head = (JSONObject) object.get("channel");
        JSONArray jsonArray = (JSONArray) head.get("item");
        return jsonArray.size() > 0;
    }

    private String getRandomStartWord() {
        val random = new Random();
        int number = random.nextInt(gameStartWord.size());
        return gameStartWord.get(number);
    }

    private final List<String> gameStartWord = List.of("메이커스", "고솝트", "플레이그라운드", "버디버디", "개발", "피그마", "솝트마인드", "마라탕", "음악", "디자이너", "애자일", "퇴사", "햇살티미단", "종무식", "서울", "제주", "감자", "휴지", "물고기", "책상", "햄버거", "선물", "미소", "맛집", "가방", "의자", "열정", "운동", "성장", "일기", "추억", "이야기");

}
