package org.sopt.makers.internal.member.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.makers.internal.member.domain.enums.IdeationStyle;
import org.sopt.makers.internal.member.domain.enums.WorkTime;
import org.sopt.makers.internal.member.domain.enums.CommunicationStyle;
import org.sopt.makers.internal.member.domain.enums.WorkPlace;
import org.sopt.makers.internal.member.domain.enums.FeedbackStyle;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class WorkPreference implements Serializable {

    private IdeationStyle ideationStyle;
    private WorkTime workTime;
    private CommunicationStyle communicationStyle;
    private WorkPlace workPlace;
    private FeedbackStyle feedbackStyle;

    @JsonIgnore
    public String getIdeationStyleValue() {
        return ideationStyle != null ? ideationStyle.getValue() : null;
    }

    @JsonIgnore
    public String getWorkTimeValue() {
        return workTime != null ? workTime.getValue() : null;
    }

    @JsonIgnore
    public String getCommunicationStyleValue() {
        return communicationStyle != null ? communicationStyle.getValue() : null;
    }

    @JsonIgnore
    public String getWorkPlaceValue() {
        return workPlace != null ? workPlace.getValue() : null;
    }

    @JsonIgnore
    public String getFeedbackStyleValue() {
        return feedbackStyle != null ? feedbackStyle.getValue() : null;
    }
}
