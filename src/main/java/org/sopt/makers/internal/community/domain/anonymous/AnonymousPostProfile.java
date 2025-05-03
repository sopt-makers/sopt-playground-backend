package org.sopt.makers.internal.community.domain.anonymous;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

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
public class AnonymousPostProfile extends AuditingTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "anonymous_post_profile_id")
	private Long id;


	@ManyToOne
	@JoinColumn(name = "user_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private Member member;

	@OneToOne
	@JoinColumn(name = "an_id", nullable = false)
	private AnonymousNickname nickname;

	@ManyToOne
	@JoinColumn(name = "anpi_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
	private AnonymousProfileImage profileImg;

	@OneToOne
	@JoinColumn(name = "post_id", nullable = false)
	private CommunityPost communityPost;

	@Builder
	private AnonymousPostProfile(AnonymousNickname nickname, Member member, AnonymousProfileImage profileImg, CommunityPost communityPost) {
		this.nickname = nickname;
		this.member = member;
		this.profileImg = profileImg;
		this.communityPost = communityPost;
	}
}
