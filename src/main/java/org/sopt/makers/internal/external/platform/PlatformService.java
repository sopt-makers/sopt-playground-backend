package org.sopt.makers.internal.external.platform;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.auth.AuthConfig;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PlatformService {

    private final PlatformClient platformClient;
    private final AuthConfig authConfig;

    public List<InternalUserDetails> getInternalUsers(List<Long> userIds) {
        val result = platformClient.getInternalUserDetails(
                authConfig.getPlatformApiKey(),
                authConfig.getPlatformServiceName(),
                userIds
        );

        var body = result.getBody();
        if (body == null || !Boolean.TRUE.equals(body.isSuccess())) {
            throw new NotFoundDBEntityException(
                    "플랫폼 API 호출 실패 또는 인증 오류. ids: " + userIds);
        }

        var users = body.getData();
        if (users == null || users.isEmpty()) {
            throw new NotFoundDBEntityException(
                    "플랫폼에 해당 유저 정보가 없습니다. ids: " + userIds);
        }

        return users;
    }

    public InternalUserDetails getInternalUser(Long userId) {
        val result = platformClient.getInternalUserDetails(
                authConfig.getPlatformApiKey(),
                authConfig.getPlatformServiceName(),
                Collections.singletonList(userId)
        );

        var body = result.getBody();
        if (body == null || !Boolean.TRUE.equals(body.isSuccess())) {
            throw new NotFoundDBEntityException(
                    "플랫폼 API 호출 실패 또는 인증 오류. id: " + userId);
        }

        var users = body.getData();
        if (users == null || users.isEmpty() || users.get(0) == null) {
            throw new NotFoundDBEntityException(
                    "플랫폼에 해당 유저 정보가 없습니다. id: " + userId);
        }

        return users.get(0);
    }

    public MemberSimpleResonse getMemberSimpleInfo(Long memberId) {
        InternalUserDetails userDetails = getInternalUser(memberId);
        return new MemberSimpleResonse(memberId, userDetails.name(), userDetails.profileImage());
    }
}
