package org.sopt.makers.internal.member.service;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.sopt.makers.internal.member.dto.response.SameGenerationAndPartRecommendResponse;
import org.sopt.makers.internal.member.dto.response.SameGenerationAndPartRecommendResponse.SameGenerationAndPartMember;

import lombok.extern.slf4j.Slf4j;
import org.sopt.makers.internal.exception.NotFoundException;
import org.sopt.makers.internal.external.makers.InternalUserWithMeetingUsersResponse;
import org.sopt.makers.internal.external.makers.MakersCrewClient;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.PlatformService;
import org.sopt.makers.internal.external.platform.SoptActivity;
import org.sopt.makers.internal.external.platform.UserSearchResponse;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.dto.response.MemberRecommendResponse;
import org.sopt.makers.internal.member.dto.response.MemberRecommendResponse.RecommendType;
import org.sopt.makers.internal.member.dto.response.MemberRecommendResponse.RecommendedMember;
import org.sopt.makers.internal.member.repository.MemberRepository;
import org.sopt.makers.internal.project.repository.MemberProjectRelationRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class MemberRecommendService {

    private final MemberRepository memberRepository;
    private final PlatformService platformService;
    private final MakersCrewClient makersCrewClient;
    private final MemberProjectRelationRepository memberProjectRelationRepository;
    private final Executor recommendationExecutor;

    public MemberRecommendService(
        MemberRepository memberRepository,
        PlatformService platformService,
        MakersCrewClient makersCrewClient,
        MemberProjectRelationRepository memberProjectRelationRepository,
        @Qualifier("recommendationExecutor") Executor recommendationExecutor
    ) {
        this.memberRepository = memberRepository;
        this.platformService = platformService;
        this.makersCrewClient = makersCrewClient;
        this.memberProjectRelationRepository = memberProjectRelationRepository;
        this.recommendationExecutor = recommendationExecutor;
    }

    private static final int RECOMMENDATION_SET_SIZE = 5;
    private static final int SAME_GENERATION_AND_PART_SET_SIZE = 4;
    private static final int PLATFORM_SEARCH_LIMIT = 200;

    private enum RecommendCriterion {
        SAME_PART, SAME_CREW, SAME_MBTI, SAME_PROJECT, SAME_UNIVERSITY, SAME_GENERATION
    }

    /**
     * criterion 조회 결과를 담는 DTO.
     * platformInfoCache: 이미 플랫폼 API를 호출한 경우 결과를 보관해 중복 호출을 방지한다.
     * fallbackCandidates: SAME_CREW에서 현기수 후보가 excludeIds에 의해 모두 제외됐을 때
     *                     사용할 이전기수 후보. 현기수 우선순위를 pick 단계에서 보장하기 위해 분리 보관한다.
     */
    private record CandidatePool(
        List<Long> candidates,
        List<Long> fallbackCandidates,
        Map<Long, InternalUserDetails> platformInfoCache,
        RecommendType type
    ) {
        static CandidatePool empty(RecommendType type) {
            return new CandidatePool(List.of(), List.of(), Map.of(), type);
        }

        static CandidatePool of(List<Long> candidates, Map<Long, InternalUserDetails> cache, RecommendType type) {
            return new CandidatePool(candidates, List.of(), cache, type);
        }
    }

    // /recommend/me
    public MemberRecommendResponse getRecommendations(Long userId) {
        List<RecommendCriterion> criteria = List.of(
            RecommendCriterion.SAME_PART,
            RecommendCriterion.SAME_CREW,
            RecommendCriterion.SAME_MBTI,
            RecommendCriterion.SAME_UNIVERSITY,
            RecommendCriterion.SAME_GENERATION
        );
        return buildRecommendations(userId, criteria);
    }

    // /recommend/{userId}
    public MemberRecommendResponse getRecommendationsForUser(Long userId) {
        List<RecommendCriterion> criteria = List.of(
            RecommendCriterion.SAME_PART,
            RecommendCriterion.SAME_CREW,
            RecommendCriterion.SAME_PROJECT,
            RecommendCriterion.SAME_UNIVERSITY,
            RecommendCriterion.SAME_GENERATION
        );
        return buildRecommendations(userId, criteria);
    }

    private MemberRecommendResponse buildRecommendations(Long userId, List<RecommendCriterion> criteria) {
        Member currentMember = memberRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("해당 id의 Member를 찾을 수 없습니다."));
        InternalUserDetails currentUserDetails = platformService.getInternalUser(userId);

        Set<String> myParts = currentUserDetails.soptActivities().stream()
            .filter(SoptActivity::isSopt)
            .map(SoptActivity::part)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        int myLatestGeneration = currentUserDetails.soptActivities().stream()
            .filter(SoptActivity::isSopt)
            .mapToInt(SoptActivity::generation)
            .max()
            .orElse(-1);

        // Hibernate 세션 의존성을 lambda 밖에서 끊어 서브 스레드 접근을 안전하게 함
        String mbti = currentMember.getMbti();
        String university = currentMember.getUniversity();

        // 1. 모든 criterion 조회를 병렬 실행
        Map<RecommendCriterion, CompletableFuture<CandidatePool>> futures = new EnumMap<>(RecommendCriterion.class);
        for (RecommendCriterion criterion : criteria) {
            Supplier<CandidatePool> task = switch (criterion) {
                case SAME_PART -> () -> fetchSamePartCandidates(myParts, userId);
                case SAME_CREW -> () -> fetchSameCrewCandidates(userId);
                case SAME_MBTI -> () -> fetchSameMbtiCandidates(mbti, userId);
                case SAME_PROJECT -> () -> fetchSameProjectCandidates(userId);
                case SAME_UNIVERSITY -> () -> fetchSameUniversityCandidates(university, userId);
                case SAME_GENERATION -> () -> fetchSameGenerationCandidates(myLatestGeneration, userId);
            };
            futures.put(criterion, submitCriterion(criterion, task));
        }
        try {
            CompletableFuture.allOf(futures.values().toArray(new CompletableFuture[0]))
                .get(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.warn("멤버 추천 criterion 조회 타임아웃 - 완료된 결과로만 응답 구성");
        } catch (ExecutionException e) {
            log.warn("멤버 추천 criterion 조회 실패: {}", e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("멤버 추천 criterion 조회 인터럽트: {}", e.getMessage());
        }

        // 2. 우선순위 순서대로 중복 없이 1명씩 선택 (타임아웃된 future는 건너뜀)
        Set<Long> excludeIds = new HashSet<>();
        excludeIds.add(userId);
        List<AbstractMap.SimpleEntry<Long, CandidatePool>> picks = new ArrayList<>();

        for (RecommendCriterion criterion : criteria) {
            if (picks.size() >= RECOMMENDATION_SET_SIZE) break;
            CompletableFuture<CandidatePool> future = futures.get(criterion);
            if (!future.isDone()) continue;
            CandidatePool pool = future.join();

            // primary candidates 먼저, 없을 때만 fallback 사용 (SAME_CREW 현기수/이전기수 우선순위 보장)
            List<Long> available = pool.candidates().stream()
                .filter(id -> !excludeIds.contains(id))
                .collect(Collectors.toCollection(ArrayList::new));
            if (available.isEmpty() && !pool.fallbackCandidates().isEmpty()) {
                available = pool.fallbackCandidates().stream()
                    .filter(id -> !excludeIds.contains(id))
                    .collect(Collectors.toCollection(ArrayList::new));
            }

            if (!available.isEmpty()) {
                Collections.shuffle(available);
                Long picked = available.get(0);
                picks.add(new AbstractMap.SimpleEntry<>(picked, pool));
                excludeIds.add(picked);
            }
        }

        // 3. 캐시에 없는 멤버의 platform 정보를 한 번에 배치 조회
        List<Long> needsFetch = picks.stream()
            .filter(e -> !e.getValue().platformInfoCache().containsKey(e.getKey()))
            .map(Map.Entry::getKey)
            .toList();
        Map<Long, InternalUserDetails> batchResultTemp;
        try {
            batchResultTemp = needsFetch.isEmpty()
                ? Collections.emptyMap()
                : platformService.getInternalUserDetailsMap(needsFetch);
        } catch (Exception e) {
            log.warn("멤버 platform 정보 배치 조회 실패: {}", e.getMessage());
            batchResultTemp = Collections.emptyMap();
        }
        final Map<Long, InternalUserDetails> batchResult = batchResultTemp;

        // 4. 최종 응답 빌드
        List<RecommendedMember> recommendations = picks.stream()
            .map(e -> {
                Long id = e.getKey();
                CandidatePool pool = e.getValue();
                InternalUserDetails details = pool.platformInfoCache().containsKey(id)
                    ? pool.platformInfoCache().get(id)
                    : batchResult.get(id);
                if (details == null) return null;
                return buildRecommendedMember(id, details, pool.type());
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(ArrayList::new));

        Collections.shuffle(recommendations);
        return new MemberRecommendResponse(recommendations);
    }

    private CandidatePool fetchSamePartCandidates(Set<String> myParts, Long excludeId) {
        if (myParts.isEmpty()) return CandidatePool.empty(RecommendType.SAME_PART);

        Set<String> normalizedMyParts = myParts.stream()
            .map(this::toPartEnumName)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        if (normalizedMyParts.isEmpty()) return CandidatePool.empty(RecommendType.SAME_PART);

        Map<Long, InternalUserDetails> platformInfoMap = new HashMap<>();
        for (String partEnumName : normalizedMyParts) {
            UserSearchResponse search = platformService.searchInternalUsers(
                null, partEnumName, null, null, PLATFORM_SEARCH_LIMIT, 0, null);
            search.profiles().forEach(d -> platformInfoMap.put(d.userId(), d));
        }

        Set<Long> hasProfileIds = memberRepository.findAllByHasProfileTrueAndIdIn(new ArrayList<>(platformInfoMap.keySet()))
            .stream().map(Member::getId).collect(Collectors.toSet());

        List<Long> candidates = platformInfoMap.keySet().stream()
            .filter(hasProfileIds::contains)
            .filter(id -> !id.equals(excludeId))
            .filter(id -> platformInfoMap.get(id).soptActivities().stream()
                .anyMatch(a -> a.isSopt() && normalizedMyParts.contains(toPartEnumName(a.part()))))
            .toList();

        return CandidatePool.of(candidates, platformInfoMap, RecommendType.SAME_PART);
    }

    private CandidatePool fetchSameCrewCandidates(Long userId) {
        InternalUserWithMeetingUsersResponse response;
        try {
            response = makersCrewClient.getRelatedUserIds(userId.intValue());
        } catch (Exception e) {
            log.warn("모임 관련 유저 조회 실패 - userId: {}, error: {}", userId, e.getMessage());
            return CandidatePool.empty(RecommendType.SAME_CREW);
        }

        Set<Long> currentIds = response.currentGenerationUserIds().stream()
            .map(InternalUserWithMeetingUsersResponse.UserOrgId::orgUserId)
            .filter(Objects::nonNull)
            .map(Integer::longValue)
            .collect(Collectors.toSet());
        currentIds.remove(userId);
        List<Long> currentCandidates = filterHasProfileMembers(currentIds);

        Set<Long> pastIds = response.pastGenerationUserIds().stream()
            .map(InternalUserWithMeetingUsersResponse.UserOrgId::orgUserId)
            .filter(Objects::nonNull)
            .map(Integer::longValue)
            .collect(Collectors.toSet());
        pastIds.remove(userId);
        List<Long> pastCandidates = filterHasProfileMembers(pastIds);

        // 현기수(primary)와 이전기수(fallback)를 분리 보관
        // excludeIds 적용은 pick 단계에서 수행하므로, 여기서 미리 필터하면 현기수가 비었을 때
        // 이전기수로 fallback해야 하는지 판단할 수 없다
        return new CandidatePool(currentCandidates, pastCandidates, Collections.emptyMap(), RecommendType.SAME_CREW);
    }

    private CandidatePool fetchSameMbtiCandidates(String currentMbti, Long excludeId) {
        if (currentMbti == null || currentMbti.isBlank()) return CandidatePool.empty(RecommendType.SAME_MBTI);

        List<Long> candidates = memberRepository.findAllByMbtiAndHasProfileTrue(currentMbti).stream()
            .map(Member::getId)
            .filter(id -> !id.equals(excludeId))
            .toList();

        return CandidatePool.of(candidates, Collections.emptyMap(), RecommendType.SAME_MBTI);
    }

    private CandidatePool fetchSameProjectCandidates(Long userId) {
        List<Long> myProjectIds = memberProjectRelationRepository.findAllByUserId(userId).stream()
            .map(relation -> relation.getProjectId())
            .toList();

        if (myProjectIds.isEmpty()) return CandidatePool.empty(RecommendType.SAME_PROJECT);

        Set<Long> candidateIds = memberProjectRelationRepository.findAllByProjectIdIn(myProjectIds).stream()
            .map(relation -> relation.getUserId())
            .filter(id -> !id.equals(userId))
            .collect(Collectors.toSet());

        List<Long> candidates = memberRepository.findAllByHasProfileTrueAndIdIn(new ArrayList<>(candidateIds)).stream()
            .map(Member::getId)
            .toList();

        return CandidatePool.of(candidates, Collections.emptyMap(), RecommendType.SAME_PROJECT);
    }

    private CandidatePool fetchSameUniversityCandidates(String currentUniversity, Long excludeId) {
        if (currentUniversity == null || currentUniversity.isBlank()) return CandidatePool.empty(RecommendType.SAME_UNIVERSITY);

        List<Long> candidates = memberRepository.findAllByUniversityAndHasProfileTrue(currentUniversity).stream()
            .map(Member::getId)
            .filter(id -> !id.equals(excludeId))
            .toList();

        return CandidatePool.of(candidates, Collections.emptyMap(), RecommendType.SAME_UNIVERSITY);
    }

    private CandidatePool fetchSameGenerationCandidates(int myLatestGeneration, Long excludeId) {
        if (myLatestGeneration < 0) return CandidatePool.empty(RecommendType.SAME_GENERATION);

        UserSearchResponse search = platformService.searchInternalUsers(
            myLatestGeneration, null, null, null, PLATFORM_SEARCH_LIMIT, 0, null);

        Map<Long, InternalUserDetails> platformInfoMap = search.profiles().stream()
            .collect(Collectors.toMap(InternalUserDetails::userId, Function.identity()));

        Set<Long> hasProfileIds = memberRepository.findAllByHasProfileTrueAndIdIn(new ArrayList<>(platformInfoMap.keySet()))
            .stream().map(Member::getId).collect(Collectors.toSet());

        List<Long> candidates = platformInfoMap.keySet().stream()
            .filter(hasProfileIds::contains)
            .filter(id -> !id.equals(excludeId))
            .filter(id -> platformInfoMap.get(id).soptActivities().stream()
                .anyMatch(a -> a.isSopt() && a.generation() == myLatestGeneration))
            .toList();

        return CandidatePool.of(candidates, platformInfoMap, RecommendType.SAME_GENERATION);
    }

    private RecommendedMember buildRecommendedMember(Long id, InternalUserDetails details, RecommendType type) {
        SoptActivity latestActivity = details.soptActivities().stream()
            .max(Comparator.comparingInt(SoptActivity::normalizedGeneration))
            .orElse(null);

        SoptActivity latestSoptActivity = details.soptActivities().stream()
            .filter(SoptActivity::isSopt)
            .max(Comparator.comparingInt(SoptActivity::generation))
            .orElse(null);

        Integer generation = latestActivity != null ? latestActivity.generation() : details.lastGeneration();
        String part = (latestActivity != null && !latestActivity.isSopt())
            ? "메이커스"
            : (latestSoptActivity != null ? latestSoptActivity.part() : null);

        return new RecommendedMember(id, details.name(), details.profileImage(), generation, part, type);
    }

    /**
     * 스레드풀 제출 단계의 RejectedExecutionException을 잡아 빈 풀 future로 변환한다.
     * supplyAsync는 executor에 태스크를 제출하는 시점에 예외를 던지므로
     * future가 생성되기 전이라 exceptionally()로 잡을 수 없다.
     */
    private CompletableFuture<CandidatePool> submitCriterion(RecommendCriterion criterion, Supplier<CandidatePool> task) {
        try {
            return CompletableFuture.supplyAsync(task, recommendationExecutor)
                .exceptionally(ex -> {
                    log.warn("criterion {} 조회 실패: {}", criterion, ex.getMessage());
                    return CandidatePool.empty(toRecommendType(criterion));
                });
        } catch (RejectedExecutionException e) {
            log.warn("criterion {} 작업 제출 거부 (스레드풀 포화): {}", criterion, e.getMessage());
            return CompletableFuture.completedFuture(CandidatePool.empty(toRecommendType(criterion)));
        }
    }

    private RecommendType toRecommendType(RecommendCriterion criterion) {
        return switch (criterion) {
            case SAME_PART -> RecommendType.SAME_PART;
            case SAME_CREW -> RecommendType.SAME_CREW;
            case SAME_MBTI -> RecommendType.SAME_MBTI;
            case SAME_PROJECT -> RecommendType.SAME_PROJECT;
            case SAME_UNIVERSITY -> RecommendType.SAME_UNIVERSITY;
            case SAME_GENERATION -> RecommendType.SAME_GENERATION;
        };
    }

    private List<Long> filterHasProfileMembers(Set<Long> participantIds) {
        if (participantIds.isEmpty()) return List.of();
        return memberRepository.findAllByHasProfileTrueAndIdIn(new ArrayList<>(participantIds)).stream()
            .map(Member::getId)
            .toList();
    }

    @Transactional(readOnly = true)
    public SameGenerationAndPartRecommendResponse getSameGenerationAndPartRecommendations(Long userId) {
        InternalUserDetails currentUserDetails = platformService.getInternalUser(userId);
        List<SoptActivity> activities = currentUserDetails.soptActivities();

        java.util.Optional<SoptActivity> latestSopt = activities.stream()
            .filter(SoptActivity::isSopt)
            .max(Comparator.comparingInt(SoptActivity::generation));

        java.util.Optional<SoptActivity> latestMakers = activities.stream()
            .filter(a -> !a.isSopt())
            .max(Comparator.comparingInt(SoptActivity::normalizedGeneration));

        if (latestMakers.isEmpty()) {
            if (latestSopt.isEmpty()) return new SameGenerationAndPartRecommendResponse(List.of());
            return new SameGenerationAndPartRecommendResponse(
                findSameGenerationAndPartSoptCandidates(userId, latestSopt.get())
            );
        }

        int soptNormalized = latestSopt.map(SoptActivity::normalizedGeneration).orElse(-1);
        int makersNormalized = latestMakers.get().normalizedGeneration();

        if (soptNormalized >= makersNormalized) {
            return new SameGenerationAndPartRecommendResponse(
                findSameGenerationAndPartSoptCandidates(userId, latestSopt.get())
            );
        }

        return new SameGenerationAndPartRecommendResponse(
            findSameGenerationAndPartMakersCandidates(userId, latestMakers.get())
        );
    }

    private List<SameGenerationAndPartMember> findSameGenerationAndPartSoptCandidates(Long excludeId, SoptActivity latestSopt) {
        int targetGeneration = latestSopt.generation();
        String targetPartEnum = toPartEnumName(latestSopt.part());
        if (targetPartEnum == null) return List.of();

        UserSearchResponse search = platformService.searchInternalUsers(
            targetGeneration, targetPartEnum, null, null, PLATFORM_SEARCH_LIMIT, 0, null);

        Map<Long, InternalUserDetails> platformInfoMap = search.profiles().stream()
            .collect(Collectors.toMap(InternalUserDetails::userId, Function.identity()));

        Set<Long> hasProfileIds = memberRepository.findAllByHasProfileTrueAndIdIn(new ArrayList<>(platformInfoMap.keySet()))
            .stream().map(Member::getId).collect(Collectors.toSet());

        List<Long> candidates = new ArrayList<>(platformInfoMap.keySet().stream()
            .filter(hasProfileIds::contains)
            .filter(id -> !id.equals(excludeId))
            .filter(id -> platformInfoMap.get(id).soptActivities().stream()
                .anyMatch(a -> a.isSopt()
                    && a.generation() == targetGeneration
                    && targetPartEnum.equals(toPartEnumName(a.part()))))
            .toList());

        Collections.shuffle(candidates);

        return candidates.stream()
            .limit(SAME_GENERATION_AND_PART_SET_SIZE)
            .map(id -> {
                InternalUserDetails details = platformInfoMap.get(id);
                String partName = details.soptActivities().stream()
                    .filter(a -> a.isSopt() && a.generation() == targetGeneration && targetPartEnum.equals(toPartEnumName(a.part())))
                    .map(SoptActivity::part)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(targetPartEnum);
                return new SameGenerationAndPartMember(id, details.name(), details.profileImage(), targetGeneration, partName);
            })
            .toList();
    }

    private List<SameGenerationAndPartMember> findSameGenerationAndPartMakersCandidates(Long excludeId, SoptActivity latestMakers) {
        int targetMakersGeneration = latestMakers.generation();

        UserSearchResponse search = platformService.searchInternalUsers(
            null, null, "MAKERS", null, PLATFORM_SEARCH_LIMIT, 0, null);

        Map<Long, InternalUserDetails> platformInfoMap = search.profiles().stream()
            .collect(Collectors.toMap(InternalUserDetails::userId, Function.identity()));

        Set<Long> hasProfileIds = memberRepository.findAllByHasProfileTrueAndIdIn(new ArrayList<>(platformInfoMap.keySet()))
            .stream().map(Member::getId).collect(Collectors.toSet());

        List<Long> candidates = new ArrayList<>(platformInfoMap.keySet().stream()
            .filter(hasProfileIds::contains)
            .filter(id -> !id.equals(excludeId))
            .filter(id -> platformInfoMap.get(id).soptActivities().stream()
                .anyMatch(a -> !a.isSopt() && a.generation() == targetMakersGeneration))
            .toList());

        Collections.shuffle(candidates);

        return candidates.stream()
            .limit(SAME_GENERATION_AND_PART_SET_SIZE)
            .map(id -> {
                InternalUserDetails details = platformInfoMap.get(id);
                return new SameGenerationAndPartMember(id, details.name(), details.profileImage(), targetMakersGeneration, "메이커스");
            })
            .toList();
    }

    private String toPartEnumName(String part) {
        if (part == null) return null;
        return switch (part.trim()) {
            case "안드로이드", "ANDROID" -> "ANDROID";
            case "iOS", "IOS" -> "IOS";
            case "서버", "SERVER" -> "SERVER";
            case "디자인", "DESIGN" -> "DESIGN";
            case "기획", "PLAN" -> "PLAN";
            case "웹", "WEB" -> "WEB";
            case "PM" -> "PM";
            case "프론트엔드", "FRONTEND" -> "FRONTEND";
            case "백엔드", "BACKEND" -> "BACKEND";
            case "마케터", "MARKETER" -> "MARKETER";
            case "리서처", "RESEARCHER" -> "RESEARCHER";
            case "오거나이저", "ORGANIZER" -> "ORGANIZER";
            case "CX" -> "CX";
            default -> null;
        };
    }
}
