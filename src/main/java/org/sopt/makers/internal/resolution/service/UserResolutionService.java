package org.sopt.makers.internal.resolution.service;

import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.repository.MemberRepository;
import org.sopt.makers.internal.resolution.domain.ResolutionTag;
import org.sopt.makers.internal.resolution.domain.UserResolution;
import org.sopt.makers.internal.resolution.dto.request.ResolutionSaveRequest;
import org.sopt.makers.internal.resolution.repository.UserResolutionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Service
@RequiredArgsConstructor
public class UserResolutionService {

	private final UserResolutionRepository userResolutionRepository;
	private final MemberRepository memberRepository;

	@Transactional
	public void createResolution(Long writerId, ResolutionSaveRequest request) {
		val member = getMemberById(writerId);
		UserResolution userResolution = UserResolution.builder()
			.member(member)
			.tagIds(ResolutionTag.getTagIds(request.tags()))
			.content(request.content()).build();
		userResolutionRepository.save(userResolution);
	}

	private Member getMemberById(Long userId) {
		return memberRepository.findById(userId).orElseThrow(
			() -> new NotFoundDBEntityException("Is not a Member"));
	}
}
