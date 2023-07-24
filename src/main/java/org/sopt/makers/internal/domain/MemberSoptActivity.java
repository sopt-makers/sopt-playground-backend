package org.sopt.makers.internal.domain;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "member_sopt_activity")
public class MemberSoptActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long memberId;

    @Column
    private String part;

    @Column
    private Integer generation;

    @Column(name = "team")
    private String team;

    public void setMemberId (Long memberId) {
        this.memberId = memberId;
    }

    public void setTeam(String team) {
        this.team = team;
    }
}
