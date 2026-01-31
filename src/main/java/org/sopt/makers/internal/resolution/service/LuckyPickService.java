package org.sopt.makers.internal.resolution.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.common.Constant;
import org.sopt.makers.internal.resolution.domain.UserResolution;
import org.sopt.makers.internal.resolution.domain.UserResolutionLuckyPick;
import org.sopt.makers.internal.resolution.dto.response.LuckyPickResponse;
import org.sopt.makers.internal.resolution.repository.UserResolutionLuckyPickRepository;
import org.sopt.makers.internal.resolution.repository.UserResolutionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LuckyPickService {

    private final UserResolutionRepository userResolutionRepository;
    private final UserResolutionLuckyPickRepository luckyPickRepository;
    private static final int WINNER_COUNT = 3;

    @Transactional
    public LuckyPickResponse checkLuckyPickResult(Long memberId) {
        Optional<UserResolutionLuckyPick> luckyPickOptional = luckyPickRepository.findByMemberId(memberId);

        if (luckyPickOptional.isEmpty()) {
            return new LuckyPickResponse(false);
        }

        UserResolutionLuckyPick luckyPick = luckyPickOptional.get();

        boolean isWinner = luckyPick.isResult();

        luckyPick.draw();

        return new LuckyPickResponse(isWinner);
    }

    @Transactional
    public void prepareLuckyPickEvent() {
        if (luckyPickRepository.count() > 0) {
            return;
        }

        List<UserResolution> resolutions = userResolutionRepository.findAllByGeneration(Constant.CURRENT_GENERATION);
        List<Long> participantIds = resolutions.stream()
            .map(resolution -> resolution.getMember().getId())
            .distinct()
            .toList();

        List<UserResolutionLuckyPick> participants = participantIds.stream()
            .map(UserResolutionLuckyPick::new)
            .collect(Collectors.toList());

        participants = luckyPickRepository.saveAll(participants);

        Collections.shuffle(participants);
        int countToPick = Math.min(WINNER_COUNT, participants.size());

        for (int i = 0; i < countToPick; i++) {
            participants.get(i).win();
        }
    }
}
