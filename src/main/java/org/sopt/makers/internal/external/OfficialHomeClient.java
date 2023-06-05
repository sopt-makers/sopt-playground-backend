package org.sopt.makers.internal.external;

import org.sopt.makers.internal.dto.sopticle.SopticleVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(value = "officialHome", url = "${internal.official.url}")
public interface OfficialHomeClient {
    @PostMapping(value = "/sopticle")
    void createSopticle(
            @RequestHeader(name = "api-key") String apiKey,
            @RequestBody SopticleVo sopticleRequest
    );
}
