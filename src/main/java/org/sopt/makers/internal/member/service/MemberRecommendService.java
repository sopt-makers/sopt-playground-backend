package org.sopt.makers.internal.member.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberRecommendService {

    private final MemberRepository memberRepository;
    private final PlatformService platformService;
    private final MakersCrewClient makersCrewClient;

    private static final int RECOMMENDATION_SET_SIZE = 5;
    private static final int PLATFORM_SEARCH_LIMIT = 200;

    private enum RecommendCriterion {
        SAME_PART, SAME_CREW, SAME_MBTI, SAME_UNIVERSITY, SAME_GENERATION
    }

    @Transactional(readOnly = true)
    public MemberRecommendResponse getRecommendations(Long userId) {
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

        Set<Long> excludeIds = new HashSet<>();
        excludeIds.add(userId);

        List<RecommendedMember> recommendations = new ArrayList<>();
        List<RecommendCriterion> criteria = List.of(
            RecommendCriterion.SAME_PART,
            RecommendCriterion.SAME_CREW,
            RecommendCriterion.SAME_MBTI,
            RecommendCriterion.SAME_UNIVERSITY,
            RecommendCriterion.SAME_GENERATION
        );

        // criteriaPointer는 슬롯 간 공유 - 한 번 소진된 기준은 재사용하지 않음
        int criteriaPointer = 0;
        for (int slot = 0; slot < RECOMMENDATION_SET_SIZE && criteriaPointer < criteria.size(); slot++) {
            boolean isFound = false;
            while (criteriaPointer < criteria.size() && !isFound) {
                RecommendCriterion currentCriteria = criteria.get(criteriaPointer++);
                Optional<RecommendedMember> result = switch (currentCriteria) {
                    case SAME_PART -> findSamePartCandidate(myParts, excludeIds);
                    case SAME_CREW -> findSameCrewCandidate(userId, excludeIds);
                    case SAME_MBTI -> findSameMbtiCandidate(currentMember.getMbti(), excludeIds);
                    case SAME_UNIVERSITY -> findSameUniversityCandidate(currentMember.getUniversity(), excludeIds);
                    case SAME_GENERATION -> findSameGenerationCandidate(myLatestGeneration, excludeIds);
                };
                if (result.isPresent()) {
                    recommendations.add(result.get());
                    excludeIds.add(result.get().id());
                    isFound = true;
                }
            }
        }

        return new MemberRecommendResponse(recommendations);
    }

    private Optional<RecommendedMember> findSamePartCandidate(
        Set<String> myParts,
        Set<Long> excludeIds
    ) {
        if (myParts.isEmpty()) return Optional.empty();

        Set<String> normalizedMyParts = myParts.stream()
            .map(this::toPartEnumName)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        if (normalizedMyParts.isEmpty()) return Optional.empty();

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
            .filter(id -> !excludeIds.contains(id))
            .filter(id -> platformInfoMap.get(id).soptActivities().stream()
                .anyMatch(a -> a.isSopt() && normalizedMyParts.contains(toPartEnumName(a.part()))))
            .toList();

        return pickAndBuild(candidates, platformInfoMap, RecommendType.SAME_PART);
    }

    private Optional<RecommendedMember> findSameCrewCandidate(
        Long userId,
        Set<Long> excludeIds
    ) {
        InternalUserWithMeetingUsersResponse response;
        try {
            response = makersCrewClient.getRelatedUserIds(userId.intValue());
        } catch (Exception e) {
            log.warn("모임 관련 유저 조회 실패 - userId: {}, error: {}", userId, e.getMessage());
            return Optional.empty();
        }

        List<Long> currentCandidates = filterHasProfileMembers(
            response.currentGenerationUserIds().stream()
                .map(InternalUserWithMeetingUsersResponse.UserOrgId::orgUserId)
                .filter(Objects::nonNull)
                .map(Integer::longValue)
                .collect(Collectors.toSet()),
            excludeIds
        );
        if (!currentCandidates.isEmpty()) {
            return pickAndBuild(currentCandidates, Collections.emptyMap(), RecommendType.SAME_CREW);
        }

        List<Long> pastCandidates = filterHasProfileMembers(
            response.pastGenerationUserIds().stream()
                .map(InternalUserWithMeetingUsersResponse.UserOrgId::orgUserId)
                .filter(Objects::nonNull)
                .map(Integer::longValue)
                .collect(Collectors.toSet()),
            excludeIds
        );
        if (!pastCandidates.isEmpty()) {
            return pickAndBuild(pastCandidates, Collections.emptyMap(), RecommendType.SAME_CREW);
        }

        return Optional.empty();
    }

    private Optional<RecommendedMember> findSameMbtiCandidate(
        String currentMbti,
        Set<Long> excludeIds
    ) {
        if (currentMbti == null || currentMbti.isBlank()) return Optional.empty();

        List<Long> candidates = memberRepository.findAllByMbtiAndHasProfileTrue(currentMbti).stream()
            .map(Member::getId)
            .filter(id -> !excludeIds.contains(id))
            .toList();

        return pickAndBuild(candidates, Collections.emptyMap(), RecommendType.SAME_MBTI);
    }

    private Optional<RecommendedMember> findSameUniversityCandidate(
        String currentUniversity,
        Set<Long> excludeIds
    ) {
        if (currentUniversity == null || currentUniversity.isBlank()) return Optional.empty();

        List<Long> candidates = memberRepository.findAllByUniversityAndHasProfileTrue(currentUniversity).stream()
            .map(Member::getId)
            .filter(id -> !excludeIds.contains(id))
            .toList();

        return pickAndBuild(candidates, Collections.emptyMap(), RecommendType.SAME_UNIVERSITY);
    }

    private Optional<RecommendedMember> findSameGenerationCandidate(
        int myLatestGeneration,
        Set<Long> excludeIds
    ) {
        if (myLatestGeneration < 0) return Optional.empty();

        UserSearchResponse search = platformService.searchInternalUsers(
            myLatestGeneration, null, null, null, PLATFORM_SEARCH_LIMIT, 0, null);

        Map<Long, InternalUserDetails> platformInfoMap = search.profiles().stream()
            .collect(Collectors.toMap(InternalUserDetails::userId, Function.identity()));

        Set<Long> hasProfileIds = memberRepository.findAllByHasProfileTrueAndIdIn(new ArrayList<>(platformInfoMap.keySet()))
            .stream().map(Member::getId).collect(Collectors.toSet());

        List<Long> candidates = platformInfoMap.keySet().stream()
            .filter(hasProfileIds::contains)
            .filter(id -> !excludeIds.contains(id))
            .filter(id -> platformInfoMap.get(id).soptActivities().stream()
                .anyMatch(a -> a.isSopt() && a.generation() == myLatestGeneration))
            .toList();

        return pickAndBuild(candidates, platformInfoMap, RecommendType.SAME_GENERATION);
    }

    private Optional<RecommendedMember> pickAndBuild(
        List<Long> candidates,
        Map<Long, InternalUserDetails> cachedPlatformInfo,
        RecommendType type
    ) {
        if (candidates.isEmpty()) return Optional.empty();

        List<Long> shuffled = new ArrayList<>(candidates);
        Collections.shuffle(shuffled);
        Long picked = shuffled.get(0);

        InternalUserDetails details = Optional.ofNullable(cachedPlatformInfo.get(picked))
            .orElseGet(() -> platformService.getInternalUser(picked));

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

        return Optional.of(new RecommendedMember(
            picked,
            details.name(),
            details.profileImage(),
            generation,
            part,
            type
        ));
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

    private List<Long> filterHasProfileMembers(Set<Long> participantIds, Set<Long> excludeIds) {
        List<Long> filtered = participantIds.stream()
            .filter(id -> !excludeIds.contains(id))
            .toList();

        return memberRepository.findAllByHasProfileTrueAndIdIn(filtered).stream()
            .map(Member::getId)
            .toList();
    }
}
