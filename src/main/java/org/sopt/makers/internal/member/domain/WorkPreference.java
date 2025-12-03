package org.sopt.makers.internal.member.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class WorkPreference implements Serializable {

    private String ideationStyle; // "즉흥" or "숙고"
    private String workTime; // "아침" or "밤"
    private String communicationStyle; // "몰아서" or "나눠서"
    private String workPlace; // "카공" or "집콕"
    private String feedbackStyle; // "직설적" or "돌려서"

    public void updatePreference(String ideationStyle, String workTime, String communicationStyle,
                                  String workPlace, String feedbackStyle) {
        this.ideationStyle = ideationStyle;
        this.workTime = workTime;
        this.communicationStyle = communicationStyle;
        this.workPlace = workPlace;
        this.feedbackStyle = feedbackStyle;
    }
}
