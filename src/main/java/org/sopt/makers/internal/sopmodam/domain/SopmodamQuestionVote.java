package org.sopt.makers.internal.sopmodam.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.makers.internal.common.AuditingTimeEntity;
import org.sopt.makers.internal.member.domain.Member;

import jakarta.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(
    name = "sopmodam_question_vote",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_sopmodam_question_vote_round_member",
        columnNames = {"round_id", "member_id"}
    )
)
public class SopmodamQuestionVote extends AuditingTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 회차당 1인 1표를 DB 유니크 제약으로 보장하기 위해 질문의 회차를 함께 저장한다
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", nullable = false)
    private SopmodamRound round;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private SopmodamQuestion question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
}
