package org.sopt.makers.internal.external.platform;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.auth.AuthConfig;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PlatformService {

    private final PlatformClient platformClient;
    private final AuthConfig authConfig;

    public List<InternalUserDetails> getInternalUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            throw new IllegalArgumentException("[InternalUserInfo 에러] 요청한 userIds 파라미터는 null이거나 빈 리스트일 수 없습니다.");
        }

        val result = platformClient.getInternalUserDetails(
                authConfig.getPlatformApiKey(),
                authConfig.getPlatformServiceName(),
                userIds
        );

        var body = result.getBody();
        if (body == null || !Boolean.TRUE.equals(body.isSuccess())) {
            throw new NotFoundDBEntityException("[InternalUserInfo 에러] 플랫폼 API 호출 실패 또는 인증 오류. ids: " + userIds);
        }

        var users = body.getData();
        if (users == null || users.isEmpty()) {
            throw new NotFoundDBEntityException("[InternalUserInfo 에러] 플랫폼에 해당 유저 정보가 없습니다. ids: " + userIds);
        }

        return users;
    }

    public InternalUserDetails getInternalUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("[InternalUserInfo 에러] 요청한 userIds 파라미터는 null이거나 빈 리스트일 수 없습니다.");
        }

        val result = platformClient.getInternalUserDetails(
                authConfig.getPlatformApiKey(),
                authConfig.getPlatformServiceName(),
                Collections.singletonList(userId)
        );

        var body = result.getBody();
        if (body == null || !Boolean.TRUE.equals(body.isSuccess())) {
            throw new NotFoundDBEntityException("[InternalUserInfo 에러] 플랫폼 API 호출 실패 또는 인증 오류. id: " + userId);
        }

        var users = body.getData();
        if (users == null || users.isEmpty() || users.get(0) == null) {
            throw new NotFoundDBEntityException("[InternalUserInfo 에러] 플랫폼에 해당 유저 정보가 없습니다. id: " + userId);
        }

        return users.get(0);
    }

    public MemberSimpleResonse getMemberSimpleInfo(Long memberId) {
        InternalUserDetails userDetails = getInternalUser(memberId);
        return new MemberSimpleResonse(memberId, userDetails.name(), userDetails.profileImage());
    }

    public List<String> getPartAndGenerationList(Long userId) {
        InternalUserDetails userDetails = getInternalUser(userId);
        List<SoptActivity> soptActivities = userDetails.soptActivities();
        return soptActivities.stream()
                .map(activity -> String.format("%d기 %s", activity.generation(), activity.part()))
                .toList();
    }

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
}
