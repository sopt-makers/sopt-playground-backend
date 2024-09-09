package org.sopt.makers.internal.domain.member;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.common.AuditingTimeEntity;

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

    public MemberReport newInstance(Member reporter, Member reportedMember, String reason) {
        return new MemberReport(reporter, reportedMember, reason);
    }

    private MemberReport(Member reporter, Member reportedMember, String reason) {
        this.reporter = reporter;
        this.reportedMember = reportedMember;
        this.reason = reason;
    }
}
