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
@Table(name = "member_question")
public class MemberQuestion extends AuditingTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "question_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "receiver_id", nullable = false)
	private Member receiver;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "asker_id")
	private Member asker;

	@Column(columnDefinition = "TEXT", nullable = false, length = 2000)
	private String content;

	@Column(nullable = false)
	private Boolean isAnonymous;

	@Column(length = 100)
	private String latestSoptActivity;

	@Builder.Default
	@Column(nullable = false)
	private Boolean isReported = false;

	@OneToOne(mappedBy = "question", cascade = CascadeType.REMOVE, orphanRemoval = true)
	private MemberAnswer answer;

	@Builder.Default
	@OneToMany(mappedBy = "question", cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<QuestionReaction> reactions = new ArrayList<>();

	public void updateContent(String content) {
		this.content = content;
	}

	public void markAsReported() {
		this.isReported = true;
	}

	public boolean hasAnswer() {
		return this.answer != null;
	}
}
