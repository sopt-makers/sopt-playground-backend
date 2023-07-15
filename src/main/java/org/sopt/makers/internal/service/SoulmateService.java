package org.sopt.makers.internal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.SmsSender;
import org.sopt.makers.internal.domain.soulmate.Soulmate;
import org.sopt.makers.internal.domain.soulmate.SoulmateMissionHistory;
import org.sopt.makers.internal.domain.soulmate.SoulmateState;
import org.sopt.makers.internal.dto.auth.NaverSmsRequest;
import org.sopt.makers.internal.dto.soulmate.MissionUpdateRequest;
import org.sopt.makers.internal.dto.soulmate.SoulmateMatchingVo;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.exception.SoulmateException;
import org.sopt.makers.internal.repository.MemberRepository;
import org.sopt.makers.internal.repository.SoulmateMissionHistoryRepository;
import org.sopt.makers.internal.repository.SoulmateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;


@RequiredArgsConstructor
@Service
@Slf4j
public class SoulmateService {
    private final SmsSender smsSender;
    private final MemberRepository memberRepository;

    private final SoulmateRepository soulmateRepository;

    private final SoulmateMissionHistoryRepository soulmateMissionHistoryRepository;
    private final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Transactional
    public Soulmate checkState (Long userId, Long soulmateId) {
        val soulmate = soulmateRepository.findByMateIdAndId(userId, soulmateId).orElseThrow(() -> new SoulmateException("NotMySoulmate"));
        if (!SoulmateState.MatchingReady.equals(soulmate.getState())) {
            val opponentSoulmate = soulmateRepository.findByMateIdAndStateIsNotOrMateIdAndStateIsNot(
                    soulmate.getOpponentId(), SoulmateState.OpenProfile,
                    soulmate.getOpponentId(), SoulmateState.Disconnected
            ).orElseThrow(() -> new NotFoundDBEntityException("Soulmate"));
            val isOpponentResponseBeforeOneDay = opponentSoulmate.getStateModifiedAt().isBefore(LocalDateTime.now(KST).minusDays(1));
            val isMyResponseBeforeOneDay = soulmate.getStateModifiedAt().isBefore(LocalDateTime.now(KST).minusDays(1));
            if (isOpponentResponseBeforeOneDay || isMyResponseBeforeOneDay) {
                soulmate.changeState(SoulmateState.Disconnected, getNow());
                opponentSoulmate.changeState(SoulmateState.Disconnected, getNow());
            }
        }
        return soulmate;
    }

    @Transactional(readOnly = true)
    public List<SoulmateMissionHistory> getMissionHistories (Long userId, Long soulmateId) {
        val soulmate = soulmateRepository.findByMateIdAndId(userId, soulmateId).orElseThrow(() -> new SoulmateException("NotMySoulmate"));
        val opponentSoulmate = soulmateRepository.findByMateIdAndStateIsNotOrMateIdAndStateIsNot(
                soulmate.getOpponentId(), SoulmateState.OpenProfile,
                soulmate.getOpponentId(), SoulmateState.Disconnected
        ).orElseThrow(() -> new NotFoundDBEntityException("Soulmate"));
        return this.soulmateMissionHistoryRepository.findAllBySoulmateIdOrSoulmateIdOrderBySentAtAsc(soulmateId, opponentSoulmate.getId());
    }

    @Transactional(readOnly = true)
    public List<Soulmate> getSoulmateHistories (Long userId) {
        return this.soulmateRepository.findAllByMateIdOrderByStartAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public String getSoulmateHint (Long opponentUserId, Integer missionSequence) {
        val opponentUser = memberRepository.findById(opponentUserId).orElseThrow(() -> new NotFoundDBEntityException("Member"));
        return switch (missionSequence) {
            case 1 -> opponentUser.getMbti();
            case 2 -> opponentUser.getActivities().get(0).getPart();
            case 3 -> opponentUser.getAddress();
            default -> opponentUser.getMbti();
        };
    }

    @Transactional
    public Soulmate missionResponded (MissionUpdateRequest request, Long senderId) {
        val soulmate = soulmateRepository.findById(request.soulmateId()).orElseThrow(() -> new NotFoundDBEntityException("Soulmate"));
        val mission = SoulmateMissionHistory.builder()
                .soulmateId(request.soulmateId())
                .senderId(senderId)
                .missionSequence(soulmate.getMissionSequence())
                .message(request.message())
                .sentAt(LocalDateTime.now(KST))
                .build();
        soulmateMissionHistoryRepository.save(mission);
        val opponentSoulmate = soulmateRepository.findByMateIdAndStateIsNot(
                soulmate.getOpponentId(), SoulmateState.OpenProfile
        ).orElseThrow(() -> new NotFoundDBEntityException("OpponentSoulmate"));
        if (SoulmateState.SelfCompleted.equals(opponentSoulmate.getState())) {
            if (soulmate.getMissionSequence() == 3) {
                soulmate.changeState(SoulmateState.OpenProfile, getNow());
                opponentSoulmate.changeState(SoulmateState.OpenProfile, getNow());
            } else {
                soulmate.bothMissionCompleted(SoulmateState.MissionOpen, getNow());
                opponentSoulmate.bothMissionCompleted(SoulmateState.MissionOpen, getNow());
            }
        } else {
            soulmate.changeState(SoulmateState.SelfCompleted, getNow());
            opponentSoulmate.changeState(SoulmateState.OpponentCompleted, getNow());
        }
        return soulmate;
    }

    @Transactional
    public SoulmateMatchingVo readyToMatching (Long userId) {
        val member = memberRepository.findById(userId).orElseThrow(() -> new NotFoundDBEntityException("Member"));
        validateAgreeToSoulmate(member);
        val soulmate = soulmateRepository.save(Soulmate.builder()
                        .mateId(member.getId())
                        .state(SoulmateState.MatchingReady)
                        .stateModifiedAt(LocalDateTime.now(KST))
                        .startAt(LocalDateTime.now(KST)).build());
        return new SoulmateMatchingVo(SoulmateState.MatchingReady, soulmate.getId());
    }

    @Transactional
    public void tryMatching () {
        val rand = new Random();
        val soulmateList = this.soulmateRepository.findAllByStateOrderByStateModifiedAtDesc(SoulmateState.MatchingReady);
        if (!soulmateList.isEmpty()) {
            val recentSoulmate = soulmateList.get(0);
            val isAfterMatchingIteration = recentSoulmate.getStateModifiedAt().isBefore(getNow().minusDays(1));
            if (!isAfterMatchingIteration) return;

            while (soulmateList.size() >= 2) {
                val mateOneId = rand.nextInt(soulmateList.size());
                val mateOne = soulmateList.get(mateOneId);
                soulmateList.remove(mateOneId);
                val mateTwoId = rand.nextInt(soulmateList.size());
                val mateTwo = soulmateList.get(mateTwoId);
                soulmateList.remove(mateTwoId);

                mateOne.changeState(SoulmateState.MissionOpen, getNow());
                mateOne.matched(mateTwo.getMateId());
                mateTwo.changeState(SoulmateState.MissionOpen, getNow());
                mateTwo.matched(mateOne.getMateId());

                val userOne = this.memberRepository.findById(mateOne.getMateId()).orElseThrow(() -> new NotFoundDBEntityException("Member"));;
                val userTwo = this.memberRepository.findById(mateOne.getOpponentId()).orElseThrow(() -> new NotFoundDBEntityException("Member"));;
                sendSMSAboutMatching(userOne.getPhone());
                sendSMSAboutMatching(userTwo.getPhone());
            }
        }
    }

    @Transactional
    public void tryTestMatching () {
        val rand = new Random();
        val soulmateList = this.soulmateRepository.findAllByStateOrderByStateModifiedAtDesc(SoulmateState.MatchingReady);
        if (!soulmateList.isEmpty()) {
            while (soulmateList.size() >= 2) {
                val mateOneId = rand.nextInt(soulmateList.size());
                val mateOne = soulmateList.get(mateOneId);
                soulmateList.remove(mateOneId);
                val mateTwoId = rand.nextInt(soulmateList.size());
                val mateTwo = soulmateList.get(mateTwoId);
                soulmateList.remove(mateTwoId);

                mateOne.changeState(SoulmateState.MissionOpen, getNow());
                mateOne.matched(mateTwo.getMateId());
                mateTwo.changeState(SoulmateState.MissionOpen, getNow());
                mateTwo.matched(mateOne.getMateId());

                val userOne = this.memberRepository.findById(mateOne.getMateId()).orElseThrow(() -> new NotFoundDBEntityException("Member"));;
                val userTwo = this.memberRepository.findById(mateOne.getOpponentId()).orElseThrow(() -> new NotFoundDBEntityException("Member"));;
                sendSMSAboutMatching(userOne.getPhone());
                sendSMSAboutMatching(userTwo.getPhone());
            }
        }
    }

    @Transactional
    public void agreeToSoulmate (Long userId) {
        val member = memberRepository.findById(userId).orElseThrow(() -> new NotFoundDBEntityException("Member"));
        if (!member.getHasProfile()) throw new SoulmateException("EmptyProfile");
        if (member.getMbti() == null || member.getAddress() == null || member.getAddress().isEmpty()) {
            throw new SoulmateException("EmptyMissionInfo");
        }
        member.agreeToUseSoulmate();
    }

    @Transactional
    public void disagreeToSoulmate (Long userId) {
        val member = memberRepository.findById(userId).orElseThrow(() -> new NotFoundDBEntityException("Member"));
        member.disagreeToUseSoulmate();
        val optionalSoulmate = soulmateRepository.findByMateIdAndStateIsNotOrMateIdAndStateIsNot(
                userId, SoulmateState.OpenProfile,
                userId, SoulmateState.Disconnected
        );
        if (optionalSoulmate.isPresent()) {
            val soulmate = optionalSoulmate.get();
            if (SoulmateState.MatchingReady.equals(soulmate.getState())) {
                soulmate.changeState(SoulmateState.Disconnected, getNow());
            } else {
                val opponentSoulmate = soulmateRepository.findByMateIdAndStateIsNotOrMateIdAndStateIsNot(
                        soulmate.getOpponentId(), SoulmateState.OpenProfile,
                        soulmate.getOpponentId(), SoulmateState.Disconnected
                ).orElseThrow(() -> new NotFoundDBEntityException("Soulmate"));
                soulmate.changeState(SoulmateState.Disconnected, getNow());
                opponentSoulmate.changeState(SoulmateState.Disconnected, getNow());
            }
        }

    }

    private void sendSMSAboutMatching (String phone) {
        if (phone == null || phone.isEmpty()) {
            log.error("유저의 전화 번호가 이상합니다.");
            return;
        }
        val message = "[SOPT Makers] Soulmate 매칭이 성사되었어요!";
        log.info(message);
        smsSender.sendSms(new NaverSmsRequest.SmsMessage(phone, message));
    }

    private void validateAgreeToSoulmate (Member member) {
        if (!member.getOpenToSoulmate()) throw new SoulmateException("NotAgreeToSoulmate");
    }

    private LocalDateTime getNow () {
        return LocalDateTime.now(KST);
    }
}
