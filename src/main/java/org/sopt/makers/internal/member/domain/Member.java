package org.sopt.makers.internal.member.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.FetchType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.sopt.makers.internal.vote.domain.VoteSelection;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@DynamicInsert
@Table(name = "users")
public class Member {

    @Id
    private Long id;

    @Column
    private String address;

    @Column
    private String university;

    @Column
    private String major;

    @Column
    private String introduction;

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

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "member", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<VoteSelection> voteSelections = new ArrayList<>();

    public void editActivityChange(Boolean isCheck) {
        this.editActivitiesAble = isCheck;
    }

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
        this.links.clear();
        this.links.addAll(links);
        this.careers.clear();
        this.careers.addAll(careers);
        this.hasProfile = true;
        this.isPhoneBlind = isPhoneBlind;
    }
}
