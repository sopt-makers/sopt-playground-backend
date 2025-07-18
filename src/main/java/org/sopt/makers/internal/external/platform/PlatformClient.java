package org.sopt.makers.internal.external.platform;

import java.util.List;
import org.sopt.makers.internal.auth.common.code.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

    // 수정 구현

}