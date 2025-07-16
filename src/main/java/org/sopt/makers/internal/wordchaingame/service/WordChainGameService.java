package org.sopt.makers.internal.wordchaingame.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.sopt.makers.internal.auth.AuthConfig;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.PlatformService;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.external.platform.MemberSimpleResonse;
import org.sopt.makers.internal.wordchaingame.domain.Word;
import org.sopt.makers.internal.wordchaingame.domain.WordChainGameRoom;
import org.sopt.makers.internal.wordchaingame.domain.WordChainGameWinner;
import org.sopt.makers.internal.wordchaingame.dto.response.WordChainGameRoomResponse;
import org.sopt.makers.internal.wordchaingame.dto.response.WordChainGameWinnerResponse;
import org.sopt.makers.internal.wordchaingame.dto.request.WordChainGameGenerateRequest;
import org.sopt.makers.internal.exception.WordChainGameHasWrongInputException;
import org.sopt.makers.internal.wordchaingame.repository.WordChainGameQueryRepository;
import org.sopt.makers.internal.wordchaingame.repository.WordChainGameRepository;
import org.sopt.makers.internal.wordchaingame.repository.WordChainGameWinnerRepository;
import org.sopt.makers.internal.wordchaingame.repository.WordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class WordChainGameService {
    private final WordRepository wordRepository;
    private final WordChainGameWinnerRepository wordChainGameWinnerRepository;
    private final WordChainGameRepository wordChainGameRepository;
    private final WordChainGameQueryRepository wordChainGameQueryRepository;

    private final PlatformService platformService;

    private final AuthConfig authConfig;

    private final String[] initialChs = {"ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ", "ㄹ", "ㅁ", "ㅂ", "ㅃ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅉ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"};
    private final String[] medialChs = {"ㅏ", "ㅐ", "ㅑ", "ㅒ", "ㅓ", "ㅔ", "ㅕ", "ㅖ", "ㅗ", "ㅘ", "ㅙ", "ㅚ", "ㅛ", "ㅜ", "ㅝ", "ㅞ", "ㅟ", "ㅠ", "ㅡ", "ㅢ", "ㅣ"};
    private final String[] finalChs = {" ", "ㄱ", "ㄲ", "ㄳ", "ㄴ", "ㄵ", "ㄶ", "ㄷ", "ㄹ", "ㄺ", "ㄻ", "ㄼ", "ㄽ", "ㄾ", "ㄿ", "ㅀ", "ㅁ", "ㅂ", "ㅄ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"};
    private final List<String> gameStartWord = List.of("메이커스", "고솝트", "플레이그라운드", "버디버디", "개발", "피그마", "솝트마인드", "마라탕", "음악", "디자이너", "애자일", "퇴사", "햇살티미단", "종무식", "서울", "제주", "감자", "휴지", "물고기", "책상", "햄버거", "선물", "미소", "맛집", "가방", "의자", "열정", "운동", "성장", "일기", "추억", "이야기");

    @Transactional
    public void createWord(Member member, WordChainGameGenerateRequest request) {
        checkWordIsOneLetter(request.word());
        checkWordIsKoreanLetter(request.word());
        checkWordIsOneLetter(request.word());
        checkRoomIsValid(request.roomId());
        checkIsChainingWord(request.roomId(), request.word());
        checkIsInDictionary(request.word());
        checkIsLastWordWriterIsMakingNextWord(request.roomId(), member.getId());
        checkDuplicateWord(request.roomId(), request.word());
        wordRepository.save(Word.builder().roomId(request.roomId()).memberId(member.getId())
                .word(request.word()).createdAt(LocalDateTime.now()).build());
    }

    @Transactional
    public WordChainGameRoom createWordGameRoom(Member member) {
        val isGameCreatedBefore = wordChainGameRepository.existsByIdIsNotNull();
        val createdUserId = isGameCreatedBefore ? member.getId() : null;
        if (isGameCreatedBefore) {
            val lastRoom = wordChainGameQueryRepository.findGameRoomOrderByCreatedDesc().get(0);
            checkInputWordIsNone(lastRoom);
            checkLastWordWriterIsMakingNewGame(lastRoom.getId(), member.getId());
            insertGameWinner(lastRoom.getId());
        }
        return wordChainGameRepository.save(WordChainGameRoom.builder()
                .createdAt(LocalDateTime.now())
                .startWord(getRandomStartWord())
                .createdUserId(createdUserId)
                .build());
    }

    @Transactional
    public void insertGameWinner(Long lastRoomId) {
        val lastWord = wordRepository.findFirstByRoomIdOrderByCreatedAtDesc(lastRoomId);
        val score = wordChainGameWinnerRepository.findFirstByUserIdOrderByIdDesc(lastWord.getMemberId());
        val userScore = Objects.isNull(score) ? 0 : score.getScore();
        wordChainGameWinnerRepository.save(WordChainGameWinner.builder()
                .roomId(lastRoomId)
                .score(userScore + 1)
                .userId(lastWord.getMemberId()).build());
    }

    @Transactional(readOnly = true)
    public List<WordChainGameRoom> getAllRoom(Integer limit, Long cursor) {
        if (limit != null) {
            if (limit == 0) limit = 1;
            return wordChainGameQueryRepository.findAllLimitedGameRoom(limit, cursor);
        } else {
            return wordChainGameQueryRepository.findAllGameRoom();
        }
    }

    @Transactional(readOnly = true)
    public List<WordChainGameWinnerResponse> getAllWinner(Integer limit, Integer cursor) {
        if (limit != null) {
            List<WordChainGameWinner> limitedWinners = wordChainGameQueryRepository.findAllLimitedWinner(limit, cursor);
            return getWinnerResponse(limitedWinners);
        } else {
            List<WordChainGameWinner> allWinners = wordChainGameWinnerRepository.findAllByOrderByIdDesc();
            return getWinnerResponse(allWinners);
        }
    }

    private List<WordChainGameWinnerResponse> getWinnerResponse(List<WordChainGameWinner> gameWinners) {
        List<Long> userIds = gameWinners.stream().map(WordChainGameWinner::getUserId).toList();
        List<InternalUserDetails> userDetails = platformService.getInternalUsers(userIds);

        Map<Long, InternalUserDetails> userDetailMap = userDetails.stream()
                .collect(Collectors.toMap(InternalUserDetails::userId, Function.identity()));

        return gameWinners.stream()
                .map(winner -> {
                    InternalUserDetails userDetail = userDetailMap.get(winner.getUserId());
                    MemberSimpleResonse userResponse = new MemberSimpleResonse(userDetail.userId(), userDetail.name(), userDetail.profileImage());
                    return new WordChainGameWinnerResponse(winner.getRoomId(), userResponse);
                }).toList();
    }

    public Map<Long, InternalUserDetails> getUserMapFromCreatedUserIds(List<WordChainGameRoom> rooms) {
        List<Long> startUserIds = rooms.stream()
                .map(WordChainGameRoom::getCreatedUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<InternalUserDetails> startUsersDetails = platformService.getInternalUsers(startUserIds);
        return startUsersDetails.stream()
                .collect(Collectors.toMap(InternalUserDetails::userId, Function.identity()));
    }

    public WordChainGameRoomResponse toRoomResponse(WordChainGameRoom room, Map<Long, InternalUserDetails> startUserMap) {
        InternalUserDetails startUserDetail = startUserMap.get(room.getCreatedUserId());
        MemberSimpleResonse startUserResponse = new MemberSimpleResonse(startUserDetail.userId(), startUserDetail.name(), startUserDetail.profileImage());

        List<Long> userIds = room.getWordList().stream()
                .map(Word::getMemberId)
                .distinct()
                .toList();

        Map<Long, InternalUserDetails> userDetailMap = platformService.getInternalUsers(userIds).stream()
                .collect(Collectors.toMap(InternalUserDetails::userId, Function.identity()));

        List<WordChainGameRoomResponse.WordResponse> wordList = room.getWordList().stream()
                .sorted(Comparator.comparing(Word::getId))
                .map(word -> {
                    InternalUserDetails userDetail = userDetailMap.get(word.getMemberId());
                    MemberSimpleResonse responseMember =  new MemberSimpleResonse(userDetail.userId(), userDetail.name(), userDetail.profileImage());
                    return new WordChainGameRoomResponse.WordResponse(word.getWord(), responseMember);
                })
                .toList();

        return new WordChainGameRoomResponse(room.getId(), room.getStartWord(), startUserResponse, wordList);
    }

    private String getRandomStartWord() {
        val random = new Random();
        int number = random.nextInt(gameStartWord.size());
        return gameStartWord.get(number);
    }

    /*
     validation 로직들
     */
    @Transactional(readOnly = true)
    public void checkIsLastWordWriterIsMakingNextWord(Long roomId, Long memberId) {
        val recentWord = wordRepository.findFirstByRoomIdOrderByCreatedAtDesc(roomId);
        if (!Objects.isNull(recentWord)) {
            boolean isLastWordWriterIsMakingNextWord = recentWord.getMemberId().equals(memberId);
            if (isLastWordWriterIsMakingNextWord)
                throw new WordChainGameHasWrongInputException("본인 단어에는 단어를 이을 수 없어요.");
        }
    }

    @Transactional(readOnly = true)
    public void checkIsChainingWord(Long roomId, String word) {
        val room = wordChainGameRepository.findById(roomId);
        val recentWord = wordRepository.findFirstByRoomIdOrderByCreatedAtDesc(roomId);
        if (Objects.isNull(recentWord) && room.isPresent()) {
            checkIsChainingWord(room.get().getStartWord(), word);
        } else {
            checkIsChainingWord(recentWord.getWord(), word);
        }
    }

    @Transactional(readOnly = true)
    public void checkLastWordWriterIsMakingNewGame(Long lastRoomId, Long memberId) {
        val lastWord = wordRepository.findFirstByRoomIdOrderByCreatedAtDesc(lastRoomId);
        val isLastWordWriterIsMakingNewGame = lastWord.getMemberId().equals(memberId);
        if (isLastWordWriterIsMakingNewGame)
            throw new WordChainGameHasWrongInputException("마지막 단어 작성자는 새로 게임을 시작할 수 없어요.");
    }

    @Transactional(readOnly = true)
    public void checkDuplicateWord(Long roomId, String word) {
        boolean hasDuplicateWord = (wordRepository.existsByWordAndRoomId(word, roomId));
        if (hasDuplicateWord) {
            throw new WordChainGameHasWrongInputException("이미 누군가 사용한 단어예요.");
        }
    }

    private boolean checkWordExistInDictionary(String search) {
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
        } catch (ParseException e) {
            e.printStackTrace();
        }
        JSONObject head = (JSONObject) object.get("channel");
        JSONArray jsonArray = (JSONArray) head.get("item");
        if (Objects.isNull(jsonArray)) return false;
        JSONObject index = (JSONObject) jsonArray.get(0);
        JSONArray sense = (JSONArray) index.get("sense");
        JSONObject senseObject = (JSONObject) sense.get(0);
        String pos = senseObject.get("pos").toString();
        return pos.equals("명사");
    }

    @Transactional(readOnly = true)
    public void checkRoomIsValid(Long roomId) {
        val room = wordChainGameRepository.findById(roomId);
        if (room.isEmpty()) throw new WordChainGameHasWrongInputException("없는 방 번호입니다.");
    }

    private void checkInputWordIsNone(WordChainGameRoom lastRoom) {
        val noInputWordInRoom = lastRoom.getWordList().isEmpty();
        if (noInputWordInRoom)
            throw new WordChainGameHasWrongInputException("이전 게임에 아무도 답을 하지 않은 경우에는 새로운 방을 만들 수 없어요.");
    }

    private void checkWordIsKoreanLetter(String word) {
        if (!word.matches("[ㄱ-ㅎㅏ-ㅣ가-힣]+")) throw new WordChainGameHasWrongInputException("한글 이외의 문자는 허용되지 않아요.");
    }

    private void checkWordIsOneLetter(String word) {
        if (word.length() < 2) throw new WordChainGameHasWrongInputException("한글자 단어는 사용할 수 없어요.");
    }

    private void checkIsInDictionary(String word) {
        val isWordInDictionary = checkWordExistInDictionary(word);
        if (!isWordInDictionary) throw new WordChainGameHasWrongInputException("표준국어대사전에 존재하지 않는 단어예요.");
    }

    private void checkIsChainingWord(String lastWord, String nextWord) {
        if (checkIsNotChainingWord(lastWord, nextWord))
            throw new WordChainGameHasWrongInputException("끝말을 잇는 단어가 아니에요.");
    }

    private boolean checkIsNotChainingWord(String lastWord, String nextWord) {
        if (checkInitialSoundIsDooemBubchik(lastWord.charAt(lastWord.length() - 1), nextWord.charAt(0))) return false;
        return nextWord.charAt(0) != lastWord.charAt(lastWord.length() - 1);
    }

    private boolean checkInitialSoundIsDooemBubchik(char lastChar, char firstChar) {
        final String neeun_to_eung = "ㅕㅛㅠㅣ";
        final String leeuel_to_eung = "ㅑㅕㅖㅛㅠㅣ";
        final String leeuel_to_neeun = "ㅏㅐㅗㅚㅜㅡ";

        int preWord = lastChar - 0xAC00;
        int initialCh = ((preWord) / (21 * 28));
        if (!Objects.equals(initialChs[initialCh], "ㄹ") && !Objects.equals(initialChs[initialCh], "ㄴ")) return false;
        int medialCh = ((preWord % (28 * 21)) / 28);
        int finalCh = ((preWord % 28));

        int nextWord = firstChar - 0xAC00;
        int initialNextCh = ((nextWord) / (21 * 28));
        int medialNextCh = ((nextWord % (28 * 21)) / 28);
        int finalNextCh = ((nextWord % 28));

        if (Objects.equals(initialChs[initialCh], "ㄴ")) {
            val canBeDooem = neeun_to_eung.contains(medialChs[medialCh]);
            if (canBeDooem) {
                val nextWordStartsWithEungOrLeeul = Objects.equals(initialChs[initialNextCh], "ㅇ") || Objects.equals(initialChs[initialNextCh], "ㄴ");
                if (nextWordStartsWithEungOrLeeul) {
                    if (Objects.equals(finalChs[finalCh], finalChs[finalNextCh]) && Objects.equals(medialChs[medialCh], medialChs[medialNextCh]))
                        return true;
                }
            }
        }

        if (Objects.equals(initialChs[initialCh], "ㄹ")) {
            val canBeDooem = leeuel_to_eung.contains(medialChs[medialCh]);
            if (canBeDooem) {
                val nextWordStartsWithEungOrLeeul = Objects.equals(initialChs[initialNextCh], "ㅇ") || Objects.equals(initialChs[initialNextCh], "ㄹ");
                if (nextWordStartsWithEungOrLeeul) {
                    if (Objects.equals(finalChs[finalCh], finalChs[finalNextCh]) && Objects.equals(medialChs[medialCh], medialChs[medialNextCh]))
                        return true;
                }
            }
        }

        if (Objects.equals(initialChs[initialCh], "ㄹ")) {
            val canBeDooem = leeuel_to_neeun.contains(medialChs[medialCh]);
            if (canBeDooem) {
                val nextWordStartsWithNeeunOrLeeul = Objects.equals(initialChs[initialNextCh], "ㅇ") || Objects.equals(initialChs[initialNextCh], "ㄴ");
                if (nextWordStartsWithNeeunOrLeeul) {
                    if (Objects.equals(finalChs[finalCh], finalChs[finalNextCh]) && Objects.equals(medialChs[medialCh], medialChs[medialNextCh]))
                        return true;
                }
            }
        }
        return false;
    }
}