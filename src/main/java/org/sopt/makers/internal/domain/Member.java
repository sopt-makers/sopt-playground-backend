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

    @Column
    private String personality;

    @Column(name = "soju_capacity")
    private Integer sojuCapacity;

    @Column(name = "interested_in")
    private String interestedIn;

    @Column(name = "pour_sauce")
    private Boolean pourSauce;

    @Column(name = "hard_peach")
    private Boolean hardPeach;

    @Column(name = "mint_choco")
    private Boolean mintChoco;

    @Column(name = "red_bean_fish")
    private Boolean redBeanFish;

    @Column
    private Boolean soju;

    @Column(name = "rice_tteok")
    private Boolean riceTteok;

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
            String personality,
            Integer sojuCapacity,
            String interestedIn,
            Boolean pourSauce,
            Boolean hardPeach,
            Boolean mintChoco,
            Boolean redBeanFish,
            Boolean soju,
            Boolean riceTteok,
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
        this.personality = personality;
        this.sojuCapacity = sojuCapacity;
        this.interestedIn = interestedIn;
        this.pourSauce = pourSauce;
        this.hardPeach = hardPeach;
        this.mintChoco = mintChoco;
        this.redBeanFish = redBeanFish;
        this.soju = soju;
        this.riceTteok = riceTteok;
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
