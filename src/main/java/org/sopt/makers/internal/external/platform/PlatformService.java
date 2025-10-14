package org.sopt.makers.internal.external.platform;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.sopt.makers.internal.auth.AuthConfig;
import org.sopt.makers.internal.auth.common.code.BaseResponse;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class PlatformService {

    private final PlatformClient platformClient;
    private final AuthConfig authConfig;

    /**
     * 단일 내부 사용자 조회
     */
    public InternalUserDetails getInternalUser(Long userId) {
        validateUserId(userId);
        List<InternalUserDetails> users = fetchInternalUsers(Collections.singletonList(userId));
        return convertUserDetailsWithRoleToTeam(users.get(0));
    }
    
    /**
     * 다중 내부 사용자 조회
     */
    public List<InternalUserDetails> getInternalUsers(List<Long> userIds) {
        validateUserIds(userIds);
        
        try {
            List<InternalUserDetails> batchUsers = fetchInternalUsersBatch(userIds);
            // role을 team으로 변환하여 반환
            return batchUsers.stream()
                .map(this::convertUserDetailsWithRoleToTeam)
                .toList();
        } catch (Exception e) {
            log.warn("[INTERNAL-PLATFORM] 유저 정보 조회 실패. 요청 ID 리스트: {}, 에러: {}", userIds, e.getMessage());
        }

        return Collections.emptyList();
    }

    /**
     * userId 유효성 검증
     */
    private void validateUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            throw new IllegalArgumentException("[INTERNAL-400-EMPTY-LIST] 요청한 userIds 파라미터는 null이거나 빈 리스트일 수 없습니다.");
        }
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("[INTERNAL-400-NULL-ID] 요청한 userId 파라미터는 null일 수 없습니다.");
        }
    }

    /**
     * 플랫폼 API 호출 및 응답 처리하는 공통 로직
     */
    private List<InternalUserDetails> fetchInternalUsersBatch(List<Long> userIds) {
        val result = platformClient.getBatchInternalUserDetails(
                authConfig.getPlatformApiKey(),
                authConfig.getPlatformServiceName(),
                new GetPlatformUserBatchRequest(userIds)
        );

        var body = result.getBody();
        if (body == null || !Boolean.TRUE.equals(body.isSuccess())) {
            throw new NotFoundDBEntityException( "[INTERNAL-500] 플랫폼 API 호출 실패 또는 인증 오류. 요청 ID: " + userIds);
        }

        var users = body.getData();
        if (users == null || users.isEmpty()) {
            throw new NotFoundDBEntityException( "[INTERNAL-404-ALL] 플랫폼에 해당 유저 정보가 없습니다. 요청 ID: " + userIds);
        }

        // 누락된 userId 탐색
        Set<Long> foundIds = users.stream()
                .map(InternalUserDetails::userId)
                .collect(Collectors.toSet());

        List<Long> missingIds = userIds.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();

        if (!missingIds.isEmpty()) {
            // Log 만 남기도록 수정
            log.warn("[INTERNAL-PLATFORM] 일부 유저 정보 누락. 요청 ID: {}, 누락 ID: {}", userIds, missingIds);
        }

        return users;
    }

    /**
     * 플랫폼 API 호출 및 응답 처리하는 공통 로직
     */
    private List<InternalUserDetails> fetchInternalUsers(List<Long> userIds) {
        val result = platformClient.getInternalUserDetails(
                authConfig.getPlatformApiKey(),
                authConfig.getPlatformServiceName(),
                userIds
        );

        var body = result.getBody();
        if (body == null || !Boolean.TRUE.equals(body.isSuccess())) {
            throw new NotFoundDBEntityException( "[INTERNAL-500] 플랫폼 API 호출 실패 또는 인증 오류. 요청 ID: " + userIds);
        }

        var users = body.getData();
        if (users == null || users.isEmpty()) {
            throw new NotFoundDBEntityException( "[INTERNAL-404-ALL] 플랫폼에 해당 유저 정보가 없습니다. 요청 ID: " + userIds);
        }

        // 누락된 userId 탐색
        Set<Long> foundIds = users.stream()
                .map(InternalUserDetails::userId)
                .collect(Collectors.toSet());

        List<Long> missingIds = userIds.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();

        if (!missingIds.isEmpty()) {
            // Log 만 남기도록 수정
            log.warn("[INTERNAL-PLATFORM] 일부 유저 정보 누락. 요청 ID: {}, 누락 ID: {}", userIds, missingIds);
        }

        return users;
    }

    /**
     * SoptActivity의 role을 team으로 변환
     */
    private SoptActivity convertRoleToTeam(SoptActivity activity) {
        String teamValue = convertRoleToTeamValue(activity.role(), activity.part(), activity.team());
        return new SoptActivity(
            activity.activityId(),
            activity.generation(),
            activity.part(),
            teamValue,
            activity.role()
        );
    }

    /**
     * role을 team 값으로 변환하는 로직
     */
    private String convertRoleToTeamValue(String role, String part, String originalTeam) {
        if (role == null) {
            return originalTeam;
        }

        // MEMBER이고 team 정보가 있으면 기존 team 정보 반환
        if ("MEMBER".equals(role) && originalTeam != null && !originalTeam.isEmpty()) {
            return originalTeam;
        }

        return switch (role) {
            case "MEMBER" -> null; // 일반 회원이고 team 정보가 없는 경우
            case "PRESIDENT" -> "회장";
            case "VICE_PRESIDENT" -> "부회장";
            case "GENERAL_AFFAIRS" -> "총무";
            case "PART_LEADER" -> part + " 파트장"; // 예: "기획 파트장", "디자인 파트장"
            case "TEAM_LEADER" -> originalTeam + " 팀장"; // 예: "운영팀 팀장", "미디어팀 팀장"
            default -> originalTeam;
        };
    }

    /**
     * InternalUserDetails의 soptActivities를 role 기반으로 team 변환
     */
    private InternalUserDetails convertUserDetailsWithRoleToTeam(InternalUserDetails userDetails) {
        List<SoptActivity> convertedActivities = userDetails.soptActivities().stream()
            .map(this::convertRoleToTeam)
            .toList();
        
        return new InternalUserDetails(
            userDetails.userId(),
            userDetails.name(),
            userDetails.profileImage(),
            userDetails.birthday(),
            userDetails.phone(),
            userDetails.email(),
            userDetails.lastGeneration(),
            convertedActivities
        );
    }

    /**
     * 멤버 간단 정보 조회 (자주 쓰이는 간략 DTO로 변환)
     */
    public MemberSimpleResonse getMemberSimpleInfo(Long memberId) {
        InternalUserDetails userDetails = getInternalUser(memberId);
        return new MemberSimpleResonse(memberId, userDetails.name(), userDetails.profileImage());
    }

    /**
     * 파트 및 기수 리스트 추출
     */
    public List<String> getPartAndGenerationList(Long userId) {
        InternalUserDetails userDetails = getInternalUser(userId);
        List<SoptActivity> soptActivities = userDetails.soptActivities();
        return soptActivities.stream()
                .map(activity -> String.format("%d기 %s", activity.generation(), activity.part()))
                .toList();
    }

    /**
     * 내부 사용자 정보 업데이트
     */
    public void updateInternalUser(Long userId, PlatformUserUpdateRequest request) {
        val result = platformClient.updateUser(
                authConfig.getPlatformApiKey(),
                authConfig.getPlatformServiceName(),
                userId,
                request
        );

        val body = result.getBody();
        if (body == null || !Boolean.TRUE.equals(body.isSuccess())) {
            throw new RuntimeException("플랫폼 API 호출 실패 또는 인증 오류. id: " + userId);
        }
    }

    /**
     * 유저 검색 관련 메서드
     */
    public UserSearchResponse searchInternalUsers(
        Integer generation, String part, String team, String name,
        Integer limit, Integer offset, String orderBy
    ) {
        ResponseEntity<BaseResponse<UserSearchResponse>> result = platformClient.searchUsers(
            authConfig.getPlatformApiKey(),
            authConfig.getPlatformServiceName(),
            generation, part, team, name, limit, offset, orderBy
        );

        BaseResponse<UserSearchResponse> body = result.getBody();
        if (body == null || !body.isSuccess() || body.getData() == null) {
            throw new RuntimeException("플랫폼 검색 API 호출 실패 또는 인증 오류.");
        }
        return body.getData();
    }
}
