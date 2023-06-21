package org.sopt.makers.internal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.sopt.makers.internal.config.AuthConfig;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.Word;
import org.sopt.makers.internal.domain.WordChainGameRoom;
import org.sopt.makers.internal.domain.WordChainGameWinner;
import org.sopt.makers.internal.dto.wordChainGame.WinnerDao;
import org.sopt.makers.internal.dto.wordChainGame.WinnerVo;
import org.sopt.makers.internal.dto.wordChainGame.WordChainGameGenerateRequest;
import org.sopt.makers.internal.exception.WordChainGameHasWrongInputException;
import org.sopt.makers.internal.repository.WordChainGameQueryRepository;
import org.sopt.makers.internal.repository.WordChainGameRepository;
import org.sopt.makers.internal.repository.WordChainGameWinnerRepository;
import org.sopt.makers.internal.repository.WordRepository;
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
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class WordChainGameService {
    private final AuthConfig authConfig;
    private final WordRepository wordRepository;
    private final WordChainGameWinnerRepository wordChainGameWinnerRepository;
    private final WordChainGameRepository wordChainGameRepository;
    private final WordChainGameQueryRepository wordChainGameQueryRepository;

    @Transactional
    public Word createWord(Member member, WordChainGameGenerateRequest request) {
        val word = request.word();
        val room = wordChainGameRepository.findById(request.roomId());
        if(room.isEmpty()) throw new WordChainGameHasWrongInputException("없는 방 번호입니다.");
        val hasDuplicateWord = (wordRepository.findByWordAndRoomId(word, request.roomId()).size() > 1);
        if(hasDuplicateWord) throw new WordChainGameHasWrongInputException("이미 누군가 사용한 단어예요.");
        val recentWordList = wordRepository.findFirstByRoomIdOrderByCreatedAtDesc(request.roomId());
        if(Objects.isNull(recentWordList)) {
            if(checkIsNotChainingWord(room.get().getStartWord(), request.word())) throw new WordChainGameHasWrongInputException("끝말을 잇는 단어가 아니에요.");
        } else {
            val lastWord = recentWordList.getWord();
            if(checkIsNotChainingWord(lastWord, request.word())) throw new WordChainGameHasWrongInputException("끝말을 잇는 단어가 아니에요.");
        }
        val isWordInDictionary = checkWordExistInDictionary(word);
        if(!isWordInDictionary) throw new WordChainGameHasWrongInputException("표준국어대사전에 존재하지 않는 단어예요.");
        return wordRepository.save(Word.builder().roomId(request.roomId()).memberId(member.getId())
                .word(word).createdAt(LocalDateTime.now()).build());
    }

    @Transactional
    public WordChainGameRoom createWordGameRoom(Member member) {
        val isNotFirstGameCreated = wordChainGameRepository.count() >= 1;
        val createdUserId = isNotFirstGameCreated ? member.getId() : null;
        if (isNotFirstGameCreated) {
            val lastRoom = wordChainGameQueryRepository.findGameRoomOrderByCreatedDesc().get(0);
            val noInputWordInRoom = lastRoom.getWordList().isEmpty();
            if(!noInputWordInRoom) {
                val lastWord = wordRepository.findFirstByRoomIdOrderByCreatedAtDesc(lastRoom.getId());
                val winnerId = lastWord.getMemberId();
                val score = wordChainGameWinnerRepository.findFirstByUserIdOrderByIdDesc(winnerId);
                val userScore = Objects.isNull(score) ? 0 : score.getScore();
                wordChainGameWinnerRepository.save(WordChainGameWinner.builder()
                        .roomId(lastWord.getRoomId())
                        .score(userScore + 1)
                        .userId(winnerId).build());
            }
        }
        return wordChainGameRepository.save(WordChainGameRoom.builder()
                .createdAt(LocalDateTime.now())
                .startWord(getRandomStartWord())
                .createdUserId(createdUserId)
                .build());
    }

    @Transactional(readOnly = true)
    public List<WordChainGameRoom> getAllRoom(Integer limit, Integer cursor) {
        if(limit != null) {
            return wordChainGameQueryRepository.findAllLimitedGameRoom(limit, cursor);
        }
        else {
            return wordChainGameQueryRepository.findAllGameRoom();
        }
    }

    @Transactional(readOnly = true)
    public List<WinnerVo> getAllWinner(Integer limit, Integer cursor) {
        if(limit != null) {
            return getWinnerVo(wordChainGameQueryRepository.findAllLimitedWinner(limit, cursor));
        }
        else {
            return getWinnerVo(wordChainGameQueryRepository.findAllWinner());
        }
    }

    private List<WinnerVo> getWinnerVo(List<WinnerDao> winnerList) {
        return winnerList.stream().map(winner -> {
            val user = new WinnerVo.UserResponse(winner.memberId(), winner.name(), winner.profileImage());
            return new WinnerVo(winner.roomId(), user);
        }).collect(Collectors.toList());
    }

    private boolean checkIsNotChainingWord(String lastWord, String nextWord) {
        return nextWord.charAt(0) != lastWord.charAt(lastWord.length() - 1);
    }

    private boolean checkWordExistInDictionary(String search){
        StringBuffer result = new StringBuffer();
        try {
            String apiUrl = "http://opendict.korean.go.kr/api/search?key=" + authConfig.getDictionaryKey() + "&req_type=json&q=" + search.replaceAll("[^ㄱ-ㅎㅏ-ㅣ가-힣a-zA-Z]", "");
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
        return !Objects.isNull(jsonArray);
    }

    private String getRandomStartWord() {
        val random = new Random();
        int number = random.nextInt(gameStartWord.size());
        return gameStartWord.get(number);
    }

    private final List<String> gameStartWord = List.of("메이커스", "고솝트", "플레이그라운드", "버디버디", "개발", "피그마", "솝트마인드", "마라탕", "음악", "디자이너", "애자일", "퇴사", "햇살티미단", "종무식", "서울", "제주", "감자", "휴지", "물고기", "책상", "햄버거", "선물", "미소", "맛집", "가방", "의자", "열정", "운동", "성장", "일기", "추억", "이야기");
}