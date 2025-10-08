package org.sopt.makers.internal.member.domain;

import lombok.*;

import jakarta.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "sopt_member_history")
public class SoptMemberHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "part")
    private String part;

    @Column(name = "generation")
    private Integer generation;

    @Column(name = "is_joined")
    private Boolean isJoined;

    public void makeMemberJoin() {
        this.isJoined = true;
    }
}
