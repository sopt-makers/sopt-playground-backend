package org.sopt.makers.internal.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "users")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "auth_user_id")
    private String authUserId;

    @Column(name = "idp_type")
    private String idpType;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "generation")
    private Integer generation;

    @Column(name = "profile_image")
    private String profileImage;

    @Column
    private LocalDate birthday;

    @Column
    private String phone;

    @Column
    private String address;

    @Column
    private String university;

    @Column
    private String major;

    @Column
    private String introduction;

    @Builder.Default
    @OneToMany(
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
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

    @Column(name = "is_pour_sauce_lover")
    private Boolean isPourSauceLover;

    @Column(name = "is_hard_peach_lover")
    private Boolean isHardPeachLover;

    @Column(name = "is_mint_choco_lover")
    private Boolean isMintChocoLover;

    @Column(name = "is_red_bean_lover")
    private Boolean isRedBeanLover;

    @Column(name = "is_soju_lover")
    private Boolean isSojuLover;

    @Column(name = "is_rice_tteok_lover")
    private Boolean isRiceTteokLover;

    @Column(name = "ideal_type")
    private String idealType;

    @Column(name = "self_introduction")
    private String selfIntroduction;

    @Builder.Default
    @OneToMany(
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @JoinColumn(name = "user_id")
    private List<MemberLink> links = new ArrayList<>();

    @Builder.Default
    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JoinColumn(name = "user_id")
    private List<MemberCareer> careers = new ArrayList<>();

    @Column
    private String skill;

    @Column(name = "open_to_work")
    private Boolean openToWork;

    @Column(name = "open_to_side_project")
    private Boolean openToSideProject;

    @Column(name = "allow_official")
    private Boolean allowOfficial;

    @Builder.Default
    @Column(name = "has_profile")
    private Boolean hasProfile = false;

    public void saveMemberProfile(
            String name,
            String profileImage,
            LocalDate birthday,
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
            Boolean isPourSauceLover,
            Boolean isHardPeachLover,
            Boolean isMintChocoLover,
            Boolean isRedBeanLover,
            Boolean isSojuLover,
            Boolean isRiceTteokLover,
            String idealType,
            String selfIntroduction,
            Boolean openToWork,
            Boolean openToSideProject,
            Boolean allowOfficial,
            List<MemberSoptActivity> activities,
            List<MemberLink> links,
            List<MemberCareer> careers
    ) {
        this.name = name;
        this.profileImage = profileImage;
        this.birthday = birthday;
        this.phone = phone;
        this.email =email;
        this.address = address;
        this.university = university;
        this.major = major;
        this.introduction = introduction;
        this.skill = skill;
        this.mbti = mbti;
        this.mbtiDescription = mbtiDescription;
        this.sojuCapacity = sojuCapacity;
        this.interest = interest;
        this.isPourSauceLover = isPourSauceLover;
        this.isHardPeachLover = isHardPeachLover;
        this.isMintChocoLover = isMintChocoLover;
        this.isRedBeanLover = isRedBeanLover;
        this.isSojuLover = isSojuLover;
        this.isRiceTteokLover = isRiceTteokLover;
        this.idealType = idealType;
        this.selfIntroduction = selfIntroduction;
        this.openToWork = openToWork;
        this.openToSideProject = openToSideProject;
        this.allowOfficial = allowOfficial;
        this.activities.clear(); this.activities.addAll(activities);
        this.links.clear(); this.links.addAll(links);
        this.careers.clear(); this.careers.addAll(careers);
        this.hasProfile = true;
    }
}
