package org.sopt.makers.internal.community.domain.comment;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.common.AuditingTimeEntity;

import jakarta.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
	name = "community_comment_like",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_comment_like_member_comment",
			columnNames = {"member_id", "comment_id"}
		)
	}
)
public class CommunityCommentLike extends AuditingTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "community_comment_like_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false, updatable = false)
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "comment_id", nullable = false, updatable = false)
	private CommunityComment comment;

	@Builder
	private CommunityCommentLike(Member member, CommunityComment comment) {
		this.member = member;
		this.comment = comment;
	}
}
