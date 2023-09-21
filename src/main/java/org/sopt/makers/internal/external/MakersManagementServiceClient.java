package org.sopt.makers.internal.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(value = "soptManagementServiceClient", url = "operation.api.dev.sopt.org")
public interface MakersManagementServiceClient {

    @PostMapping("/internal/api/v1/idp/members")
    void registerManagementServiceUser(
            @RequestHeader("x-api-key") String secretKey,
            @RequestHeader("x-request-from") String origin,
            @RequestBody Map<String, Object> body
    );
}
