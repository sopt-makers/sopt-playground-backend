package org.sopt.makers.internal.external.makers;

import org.sopt.makers.internal.community.dto.response.ReviewCountResponse;
import org.sopt.makers.internal.community.dto.response.SopticleScrapedResponse;
import org.sopt.makers.internal.community.dto.SopticleVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "officialHome", url = "${internal.official.url}")
public interface OfficialHomeClient {
    @PostMapping(value = "/internal/scrap")
    ResponseEntity<SopticleScrapedResponse> getSopticleScrapedData(
            @RequestHeader(name = "api-key") String apiKey,
            @RequestBody SopticleVo sopticleRequest
    );

    @GetMapping("/reviews/internal")
    ResponseEntity<ReviewCountResponse> fetchReviewCount(
            @RequestHeader(name = "api-key") String apiKey,
            @RequestParam(name = "name") String name
    );
}
