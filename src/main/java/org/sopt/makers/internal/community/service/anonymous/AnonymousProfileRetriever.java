package org.sopt.makers.internal.community.service.anonymous;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.domain.CommunityPost;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousProfile;
import org.sopt.makers.internal.community.repository.anonymous.AnonymousProfileRepository;
import org.sopt.makers.internal.member.domain.Member;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AnonymousProfileRetriever {

	private final AnonymousProfileRepository anonymousProfileRepository;

	/**
	 * Member + Post 조합으로 익명 프로필 조회
	 */
	public Optional<AnonymousProfile> findByMemberAndPost(Member member, CommunityPost post) {
		return anonymousProfileRepository.findByMemberAndPost(member, post);
	}

	/**
	 * Post ID로 단일 익명 프로필 조회 (게시글 작성자용)
	 */
	public Optional<AnonymousProfile> findByPostId(Long postId) {
		return anonymousProfileRepository.findByPostId(postId);
	}

	/**
	 * Post ID로 모든 익명 프로필 조회
	 */
	public List<AnonymousProfile> findAllByPostId(Long postId) {
		return anonymousProfileRepository.findAllByPostId(postId);
	}

	/**
	 * 최근 N개의 익명 프로필 조회
	 */
	public List<AnonymousProfile> getTopByOrderByCreatedAt(int limit) {
		return anonymousProfileRepository.findTopByOrderByIdDescWithLimit(limit);
	}

	/**
	 * 게시글에 존재하는 닉네임 목록 조회 (멘션 검증용)
	 */
	public List<String> findNicknamesByPostIdAndNicknamesIn(Long postId, List<String> nicknames) {
		return anonymousProfileRepository.findNicknamesByPostIdAndNicknamesIn(postId, nicknames);
	}
}
