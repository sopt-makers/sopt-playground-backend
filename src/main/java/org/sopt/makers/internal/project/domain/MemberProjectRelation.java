package org.sopt.makers.internal.project.domain;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "project_users")
public class MemberProjectRelation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "user_id")
    private Long userId;

    private String role;

    private String description;

    @Column(name = "is_team_member")
    private Boolean isTeamMember;

    public MemberProjectRelation updateAll (
            String role,
            String description,
            Boolean isTeamMember
    ) {
        this.role = role == null ? this.role : role;
        this.description = description == null ? this.description : description;
        this.isTeamMember = isTeamMember == null ? this.isTeamMember : isTeamMember;
        return this;
    }
}
