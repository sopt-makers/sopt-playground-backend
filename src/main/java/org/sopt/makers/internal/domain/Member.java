package org.sopt.makers.internal.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    @Builder.Default
    @OneToMany(
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @JoinColumn(name = "user_id")
    private List<MemberSoptActivity> activities = new ArrayList<>();

    @Builder.Default
    @OneToMany(
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @JoinColumn(name = "user_id")
    private List<MemberLink> links = new ArrayList<>();

    @Column
    private String introduction;

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
            Boolean openToWork,
            Boolean openToSideProject,
            Boolean allowOfficial,
            List<MemberSoptActivity> activities,
            List<MemberLink> links
    ) {
        this.name = Objects.requireNonNullElse(name, this.name);
        this.profileImage = Objects.requireNonNullElse(profileImage, this.profileImage);
        this.birthday = Objects.requireNonNullElse(birthday, this.birthday);
        this.phone = Objects.requireNonNullElse(phone, this.phone);
        this.email =Objects.requireNonNullElse(email, this.email);
        this.address = Objects.requireNonNullElse(address, this.address);
        this.university = Objects.requireNonNullElse(university, this.university);
        this.major = Objects.requireNonNullElse(major, this.major);
        this.introduction = Objects.requireNonNullElse(introduction, this.introduction);
        this.skill = Objects.requireNonNullElse(skill, this.skill);
        this.openToWork = Objects.requireNonNullElse(openToWork, this.openToWork);
        this.openToSideProject = Objects.requireNonNullElse(openToSideProject, this.openToSideProject);
        this.allowOfficial = Objects.requireNonNullElse(allowOfficial, this.allowOfficial);
        this.activities.clear(); this.activities.addAll(activities);
        this.links.clear(); this.links.addAll(links);
        this.hasProfile = true;
    }
}
