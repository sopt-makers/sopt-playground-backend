package org.sopt.makers.internal.member.service.workpreference;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.WorkPreference;
import org.sopt.makers.internal.member.domain.enums.*;
import org.sopt.makers.internal.member.dto.request.WorkPreferenceUpdateRequest;
import org.sopt.makers.internal.member.repository.MemberRepository;
import org.sopt.makers.internal.member.service.MemberRetriever;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class WorkPreferenceModifier {

    private final MemberRepository memberRepository;
    private final MemberRetriever memberRetriever;

    @Transactional
    public void updateWorkPreference(Long memberId, WorkPreferenceUpdateRequest request) {
        Member member = memberRetriever.findMemberById(memberId);

        WorkPreference workPreference = buildWorkPreferenceFromRequest(request);
        member.updateWorkPreference(workPreference);

        memberRepository.save(member);
    }

    @Transactional
    public void saveWorkPreference(Member member, WorkPreference workPreference) {
        member.updateWorkPreference(workPreference);
        memberRepository.save(member);
    }

    public WorkPreference buildWorkPreferenceFromRequest(WorkPreferenceUpdateRequest request) {
        return WorkPreference.builder()
                .ideationStyle(IdeationStyle.fromValue(request.ideationStyle()))
                .workTime(WorkTime.fromValue(request.workTime()))
                .communicationStyle(CommunicationStyle.fromValue(request.communicationStyle()))
                .workPlace(WorkPlace.fromValue(request.workPlace()))
                .feedbackStyle(FeedbackStyle.fromValue(request.feedbackStyle()))
                .build();
    }

    public WorkPreference buildWorkPreferenceFromStrings(String ideationStyle, String workTime,
                                                         String communicationStyle, String workPlace,
                                                         String feedbackStyle) {
        if (ideationStyle == null && workTime == null && communicationStyle == null
                && workPlace == null && feedbackStyle == null) {
            return null;
        }

        return WorkPreference.builder()
                .ideationStyle(IdeationStyle.fromValue(ideationStyle))
                .workTime(WorkTime.fromValue(workTime))
                .communicationStyle(CommunicationStyle.fromValue(communicationStyle))
                .workPlace(WorkPlace.fromValue(workPlace))
                .feedbackStyle(FeedbackStyle.fromValue(feedbackStyle))
                .build();
    }
}