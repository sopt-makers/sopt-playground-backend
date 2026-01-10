package org.sopt.makers.internal.member.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.makers.internal.common.AuditingTimeEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "member_answer")
public class MemberAnswer extends AuditingTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "answer_id")
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "question_id", nullable = false, unique = true)
	private MemberQuestion question;

	@Column(columnDefinition = "TEXT", nullable = false, length = 2000)
	private String content;

	@Builder.Default
	@OneToMany(mappedBy = "answer", cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<AnswerReaction> reactions = new ArrayList<>();

	public void updateContent(String content) {
		this.content = content;
	}
}
