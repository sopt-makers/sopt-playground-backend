package org.sopt.makers.internal.dto.member;

import static org.sopt.makers.internal.common.Constant.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.sopt.makers.internal.domain.MemberSoptActivity;

@Slf4j
public record MemberProfileUpdateRequest (
        @Schema(required = true)
        String name,
        String profileImage,
        LocalDate birthday,
        @Pattern(regexp = PHONE_NUMBER_REGEX, message = "잘못된 전화번호 형식입니다. '-'을 제외한 11자의 번호를 입력해주세요.")
        String phone,
        String email,
        String address,
        String university,
        String major,
        String introduction,
        String skill,
        String mbti,
        String mbtiDescription,
        Double sojuCapacity,
        String interest,
        UserFavorRequest userFavor,
        String idealType,
        String selfIntroduction,
        List<MemberLinkUpdateRequest> links,
        @Schema(required = true)
        List<MemberSoptActivityUpdateRequest> activities,
        List<MemberCareerUpdateRequest> careers,
        Boolean allowOfficial,
        Boolean isPhoneBlind,

        @NotNull(message = "커피챗 활성 여부가 입력되지 않았습니다.")
        Boolean isCoffeeChatActivate,

        @NotBlank(message = "커피챗 설명란은 공란일 수 없습니다.")
        @Size(max = 40, message = "커피챗 설명란은 40자를 초과할 수 없습니다.")
        String coffeeChatBio
){

    public record UserFavorRequest(
            Boolean isPourSauceLover,
            Boolean isHardPeachLover,
            Boolean isMintChocoLover,
            Boolean isRedBeanFishBreadLover,
            Boolean isSojuLover,
            Boolean isRiceTteokLover
    ){}
    public record MemberLinkUpdateRequest(
            Long id,
            String title,
            String url
    ){}

    public record MemberSoptActivityUpdateRequest(
            Long id,
            Integer generation,
            String part,
            String team
    ){
        private boolean equalsInfo (MemberSoptActivity activity) {
            return generation.equals(activity.getGeneration()) && part.equals(activity.getPart());
        }
    }

    public record MemberCareerUpdateRequest(
            String companyName,
            String title,

            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM")
            String startDate,
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM")
            String endDate,
            Boolean isCurrent
    ){}


    public boolean compareProfileActivities (List<MemberSoptActivityUpdateRequest> requests, List<MemberSoptActivity> activities) {
        Comparator<MemberSoptActivityUpdateRequest> requestComparator = Comparator.comparingInt(r -> r.generation);
        requests.sort(requestComparator);

        Comparator<MemberSoptActivity> activityComparator = Comparator.comparingInt(MemberSoptActivity::getGeneration);
        activities.sort(activityComparator);

        if (requests.size() != activities.size()) {
            return false;
        }
        for (int i=0; i<requests.size(); i++) {
            if (!requests.get(i).equalsInfo(activities.get(i))) {
                return false;
            }
        }
        return true;
    }

}
