package org.sopt.makers.internal.external.platform;

import org.sopt.makers.internal.auth.common.code.BaseResponse;
import org.sopt.makers.internal.community.dto.SopticleVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(value = "platform", url = "${external.auth.url}")
public interface PlatformClient{

    @GetMapping(value = "/api/v1/users")
    ResponseEntity<BaseResponse<List<InternalUserDetails>>> getInternalUserDetails(
            @RequestHeader(name = "X-Api-Key") String apiKey,
            @RequestHeader(name = "X-Service-Name") String serviceName,
            @RequestParam(name = "userIds") List<Long> userIds
    );

    // 수정
    @GetMapping(value = "/api/v1/users")
    ResponseEntity<BaseResponse<InternalUserDetails>> getInternalUserDetails(
            @RequestHeader(name = "api-key") String apiKey,
            @RequestBody SopticleVo sopticleRequest
    );

    // TODO: - custom annotation 만들?
    // 어디에서 호출할까
    // 전체 커뮤니티글 조회 -> post 조회 -> 유저 id 리스트 뽑기 -> 리스트 전달 -> 플폼에서 객체 정보 받아오기




    // 2. 내 프로필 조회
    // 내 아이디를 알아야되자나 내 토큰을 통해서 아이디 알아내거나 정보 뽑아오는 api?
}