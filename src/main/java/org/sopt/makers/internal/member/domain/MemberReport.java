package org.sopt.makers.internal.member.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.makers.internal.common.AuditingTimeEntity;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberReport extends AuditingTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private Member reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_member_id", nullable = false)
    private Member reportedMember;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Builder
    private MemberReport(Member reporter, Member reportedMember, String reason) {
        this.reporter = reporter;
        this.reportedMember = reportedMember;
        this.reason = reason;
    }
}
