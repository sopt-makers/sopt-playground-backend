package org.sopt.makers.internal.community.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.makers.internal.community.dto.response.SopticleScrapedResponse;
import org.sopt.makers.internal.auth.AuthConfig;
import org.sopt.makers.internal.community.dto.SopticleVo;
import org.sopt.makers.internal.exception.SopticleException;
import org.sopt.makers.internal.external.makers.OfficialHomeClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class SopticleScrapedService {

    private final AuthConfig authConfig;
    private final OfficialHomeClient officialHomeClient;

    public SopticleScrapedResponse getSopticleMetaData(String sopticleUrl) {
        SopticleVo sopticleVo = new SopticleVo(sopticleUrl);
        ResponseEntity<SopticleScrapedResponse> response = officialHomeClient.getSopticleScrapedData(authConfig.getOfficialSopticleApiSecretKey(), sopticleVo);

        validateInternalApiResponse(response);
        return response.getBody();
    }

    private void validateInternalApiResponse(ResponseEntity<SopticleScrapedResponse> response) {
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("[SopticleException] statusCode: {}, body: {}", response.getStatusCode(), response.getBody());
            throw new SopticleException("Sopticle 생성 실패: status=" + response.getStatusCode() + response.getBody());
        }
    }
}
