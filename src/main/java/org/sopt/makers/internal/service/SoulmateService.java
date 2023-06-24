package org.sopt.makers.internal.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.domain.soulmate.SoulmateMissionHistory;
import org.sopt.makers.internal.dto.soulmate.MissionUpdateRequest;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.repository.MemberRepository;
import org.sopt.makers.internal.repository.SoulmateMissionHistoryRepository;
import org.sopt.makers.internal.repository.SoulmateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;


@RequiredArgsConstructor
@Service
public class SoulmateService {
    private final MemberRepository memberRepository;

    private final SoulmateRepository soulmateRepository;

    private final SoulmateMissionHistoryRepository soulmateMissionHistoryRepository;
    private final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Transactional
    public void missionResponded (MissionUpdateRequest request) {
        val soulmate = soulmateRepository.findById(request.soulmateId()).orElseThrow(() -> new NotFoundDBEntityException("Soulmate"));
        // soulmate state update
        val mission = SoulmateMissionHistory.builder()
                .soulmateId(request.soulmateId())
                .senderId(request.senderId())
                .missionSequence(request.missionSequence())
                .message(request.message())
                .sentAt(LocalDateTime.now(KST))
                .build();
        soulmateMissionHistoryRepository.save(mission);
    }


    @Transactional
    public void agreeToSoulmate (Long userId) {
        val user = memberRepository.findById(userId).orElseThrow(() -> new NotFoundDBEntityException("Member"));
        user.disagreeToUseSoulmate();
    }

    @Transactional
    public void disagreeToSoulmate (Long userId) {
        val user = memberRepository.findById(userId).orElseThrow(() -> new NotFoundDBEntityException("Member"));
        user.disagreeToUseSoulmate();
    }
}
