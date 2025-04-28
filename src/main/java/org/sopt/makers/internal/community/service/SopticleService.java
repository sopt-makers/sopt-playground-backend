package org.sopt.makers.internal.community.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.makers.internal.community.controller.dto.response.SopticleScrapedResponse;
import org.sopt.makers.internal.config.AuthConfig;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.MemberSoptActivity;
import org.sopt.makers.internal.domain.Part;
import org.sopt.makers.internal.dto.sopticle.SopticleVo;
import org.sopt.makers.internal.exception.SopticleException;
import org.sopt.makers.internal.external.OfficialHomeClient;
import org.sopt.makers.internal.member.repository.soptactivity.MemberSoptActivityRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class SopticleService {
    private final AuthConfig authConfig;
    private final MemberSoptActivityRepository memberSoptActivityRepository;
    private final OfficialHomeClient officialHomeClient;

    @Transactional
    public SopticleScrapedResponse createSopticle(String sopticleUrl, Member writer) {
        MemberSoptActivity activity = getLatestActivity(writer.getId());
        SopticleVo sopticleVo = buildSopticleVo(sopticleUrl, writer, activity);

        ResponseEntity<SopticleScrapedResponse> response = officialHomeClient.createSopticle(authConfig.getOfficialSopticleApiSecretKey(), sopticleVo);
        validateInternalApiResponse(response);

        return response.getBody();
    }

    private MemberSoptActivity getLatestActivity(Long memberId) {
        return memberSoptActivityRepository.findTop1ByMemberIdOrderByGenerationDesc(memberId);
    }

    private SopticleVo buildSopticleVo(String sopticleUrl, Member writer, MemberSoptActivity activity) {
        SopticleVo.SopticleUserVo userVo = new SopticleVo.SopticleUserVo(
                writer.getId(),
                writer.getName(),
                writer.getProfileImage(),
                writer.getGeneration(),
                Part.fromTitle(activity.getPart()).getKey()
        );
        return new SopticleVo(sopticleUrl, userVo);
    }

    private void validateInternalApiResponse(ResponseEntity<SopticleScrapedResponse> response) {
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("[SopticleException] statusCode: {}, body: {}", response.getStatusCode(), response.getBody());
            throw new SopticleException("Sopticle 생성 실패: status=" + response.getStatusCode());
        }
    }
}
