package org.sopt.makers.internal.member.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.makers.internal.common.AuditingTimeEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
	name = "answer_reaction",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_answer_reaction_answer_member",
			columnNames = {"answer_id", "member_id"}
		)
	}
)
public class AnswerReaction extends AuditingTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "reaction_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "answer_id", nullable = false)
	private MemberAnswer answer;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@Builder
	private AnswerReaction(MemberAnswer answer, Member member) {
		this.answer = answer;
		this.member = member;
	}
}
