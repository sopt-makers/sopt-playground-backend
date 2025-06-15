package org.sopt.makers.internal.community.service;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.makers.internal.community.dto.response.ReviewCountResponse;
import org.sopt.makers.internal.auth.AuthConfig;
import org.sopt.makers.internal.external.makers.OfficialHomeClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReviewService {

    private final OfficialHomeClient officialHomeClient;
    private final AuthConfig authConfig;

    public int fetchReviewCountByUsername(String username) {
        ResponseEntity<ReviewCountResponse> response = officialHomeClient.fetchReviewCount(
                authConfig.getOfficialSopticleApiSecretKey(), username
        );
        validateInternalApiResponse(response);

        return Objects.requireNonNull(response.getBody()).reviewCount();
    }

    private void validateInternalApiResponse(ResponseEntity<ReviewCountResponse> response) {
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("[ReviewServiceException] statusCode: {}, body: {}", response.getStatusCode(), response.getBody());
            throw new IllegalStateException("리뷰 조회 실패: status=" + response.getStatusCode());
        }
    }
}
