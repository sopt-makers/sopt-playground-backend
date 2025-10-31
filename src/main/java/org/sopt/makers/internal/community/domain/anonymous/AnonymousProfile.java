package org.sopt.makers.internal.community.domain.anonymous;

import jakarta.persistence.*;

import org.sopt.makers.internal.community.domain.CommunityPost;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.common.AuditingTimeEntity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
	name = "anonymous_profile",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_anonymous_profile_user_post",
			columnNames = {"user_id", "post_id"}
		)
	}
)
public class AnonymousProfile extends AuditingTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "anonymous_profile_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_id", nullable = false)
	private CommunityPost post;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "an_id", nullable = false)
	private AnonymousNickname nickname;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "anpi_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
	private AnonymousProfileImage profileImg;

	@Builder
	private AnonymousProfile(
		Member member,
		CommunityPost post,
		AnonymousNickname nickname,
		AnonymousProfileImage profileImg
	) {
		this.member = member;
		this.post = post;
		this.nickname = nickname;
		this.profileImg = profileImg;
	}
}
