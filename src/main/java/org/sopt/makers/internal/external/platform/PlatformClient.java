package org.sopt.makers.internal.external.platform;

import java.util.List;
import org.sopt.makers.internal.auth.common.code.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "platform", url = "${external.auth.url}")
public interface PlatformClient{

    @GetMapping(value = "/api/v1/users")
    ResponseEntity<BaseResponse<List<InternalUserDetails>>> getInternalUserDetails(
            @RequestHeader(name = "X-Api-Key") String apiKey,
            @RequestHeader(name = "X-Service-Name") String serviceName,
            @RequestParam(name = "userIds") List<Long> userIds
    );

    @PutMapping(value = "/api/v1/users/{userId}")
    ResponseEntity<BaseResponse<Void>> updateUser(
            @RequestHeader(name = "X-Api-Key") String apiKey,
            @RequestHeader(name = "X-Service-Name") String serviceName,
            @PathVariable("userId") Long userId,
            @RequestBody PlatformUserUpdateRequest body
    );

    @GetMapping(value = "/api/v1/users/search")
    ResponseEntity<BaseResponse<UserSearchResponse>> searchUsers(
        @RequestHeader(name = "X-Api-Key") String apiKey,
        @RequestHeader(name = "X-Service-Name") String serviceName,
        @RequestParam(name = "generation", required = false) Integer generation,
        @RequestParam(name = "part", required = false) String part,
        @RequestParam(name = "team", required = false) String team,
        @RequestParam(name = "name", required = false) String name,
        @RequestParam(name = "limit", required = false) Integer limit,
        @RequestParam(name = "offset", required = false) Integer offset,
        @RequestParam(name = "orderBy", required = false) String orderBy
    );
}