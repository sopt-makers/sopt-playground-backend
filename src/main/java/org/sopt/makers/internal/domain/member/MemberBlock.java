package org.sopt.makers.internal.domain.member;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.common.AuditingTimeEntity;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberBlock extends AuditingTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocker_id", nullable = false)
    private Member blocker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_id", nullable = false)
    private Member blockedMember;

    @Column(nullable = false)
    @ColumnDefault("true")
    private Boolean isBlocked = true;

    @Builder
    private MemberBlock(Member blocker, Member blockedMember, Boolean isBlocked) {
        this.blocker = blocker;
        this.blockedMember = blockedMember;
        this.isBlocked = isBlocked;
    }
}
