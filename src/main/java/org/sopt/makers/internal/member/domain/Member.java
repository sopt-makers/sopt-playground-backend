package org.sopt.makers.internal.member.domain;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.sopt.makers.internal.vote.domain.VoteSelection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@DynamicInsert
@Table(name = "users")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String address;

    @Column
    private String university;

    @Column
    private String major;

    @Column
    private String introduction;

    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private List<MemberSoptActivity> activities = new ArrayList<>();

    @Column
    private String mbti;

    @Column(name = "mbti_description")
    private String mbtiDescription;

    @Column(name = "soju_capacity")
    private Double sojuCapacity;

    @Column
    private String interest;

    @Embedded
    private UserFavor userFavor;

    @Column(name = "ideal_type")
    private String idealType;

    @Column(name = "self_introduction")
    private String selfIntroduction;

    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id")
    private List<MemberLink> links = new ArrayList<>();

    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id")
    private List<MemberCareer> careers = new ArrayList<>();

    @Column
    private String skill;

    @Column(name = "open_to_work")
    private Boolean openToWork;

    @Column(name = "open_to_side_project")
    private Boolean openToSideProject;

    @Builder.Default
    @Column(name = "allow_official")
    private Boolean allowOfficial = false;

    @Builder.Default
    @Column(name = "has_profile")
    private Boolean hasProfile = true;

    @Builder.Default
    @Column(name = "edit_activities_able")
    @ColumnDefault("true")
    private Boolean editActivitiesAble = true;

    @Builder.Default
    @Column(name = "openToSoulmate")
    private Boolean openToSoulmate = false;

    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault("true")
    private Boolean isPhoneBlind = true;

    //    해당 컬럼들은 QA 이상없으면 완전히 삭제하기
//    @Column(name = "auth_user_id")
//    private String authUserId;
//
//    @Column(name = "idp_type")
//    private String idpType;
//
//    @Column(name = "name")
//    private String name;
//
//    @Column(name = "email")
//    private String email;
//
//    @Column(name = "generation")
//    private Integer generation;
//
//    @Column(name = "profile_image")
//    private String profileImage;
//
//    @Column
//    private LocalDate birthday;
//
//    @Column
//    private String phone;

    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<VoteSelection> voteSelections = new ArrayList<>();

    public void editActivityChange(Boolean isCheck) {
        this.editActivitiesAble = isCheck;
    }

//    public void updateMemberAuth(String authUserId, String idpType) {
//        this.authUserId = authUserId;
//        this.idpType = idpType;
//    }

//    public void changeEmail(String email) {
//        this.email = email;
//    }

    public void saveMemberProfile(
            String address,
            String university,
            String major,
            String introduction,
            String skill,
            String mbti,
            String mbtiDescription,
            Double sojuCapacity,
            String interest,
            UserFavor userFavor,
            String idealType,
            String selfIntroduction,
            Boolean allowOfficial,
            List<MemberSoptActivity> activities,
            List<MemberLink> links,
            List<MemberCareer> careers,
            Boolean isPhoneBlind
    ) {
        this.address = address;
        this.university = university;
        this.major = major;
        this.introduction = introduction;
        this.skill = skill;
        this.mbti = mbti;
        this.mbtiDescription = mbtiDescription;
        this.sojuCapacity = sojuCapacity;
        this.interest = interest;
        this.userFavor = userFavor;
        this.idealType = idealType;
        this.selfIntroduction = selfIntroduction;
        this.allowOfficial = allowOfficial;
        this.activities.clear();
        this.activities.addAll(activities);
        this.links.clear();
        this.links.addAll(links);
        this.careers.clear();
        this.careers.addAll(careers);
        this.hasProfile = true;
        this.isPhoneBlind = isPhoneBlind;
    }
}
