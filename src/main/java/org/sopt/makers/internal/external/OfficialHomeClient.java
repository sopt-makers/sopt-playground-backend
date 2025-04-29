package org.sopt.makers.internal.external;

import org.sopt.makers.internal.community.controller.dto.response.SopticleScrapedResponse;
import org.sopt.makers.internal.dto.sopticle.SopticleVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(value = "officialHome", url = "${internal.official.url}")
public interface OfficialHomeClient {
    @PostMapping(value = "/sopticle")
    ResponseEntity<SopticleScrapedResponse> getSopticleScrapedData(
            @RequestHeader(name = "api-key") String apiKey,
            @RequestBody SopticleVo sopticleRequest
    );
}
