package org.sopt.makers.internal.resolution.service;

import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.repository.MemberRepository;
import org.sopt.makers.internal.resolution.domain.ResolutionTag;
import org.sopt.makers.internal.resolution.domain.UserResolution;
import org.sopt.makers.internal.resolution.dto.request.ResolutionSaveRequest;
import org.sopt.makers.internal.resolution.dto.response.ResolutionResponse;
import org.sopt.makers.internal.resolution.mapper.UserResolutionResponseMapper;
import org.sopt.makers.internal.resolution.repository.UserResolutionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserResolutionService {

	private final UserResolutionRepository userResolutionRepository;
	private final MemberRepository memberRepository;

	private final UserResolutionResponseMapper userResolutionResponseMapper;

	@Transactional(readOnly = true)
	public ResolutionResponse getResolution(Long memberId) {
		val member = getMemberById(memberId);
		val resolution = UserResolutionServiceUtil.findUserResolutionByMember(member, userResolutionRepository);
		val tags = ResolutionTag.getTagNames(resolution.getTagIds());
		return userResolutionResponseMapper.toResolutionResponse(member, tags, resolution.getContent());
	}

	@Transactional
	public void createResolution(Long writerId, ResolutionSaveRequest request) {
		val member = getMemberById(writerId);
		if (member.getGeneration() == null) {
			throw new ClientBadRequestException("Not exists profile info");
		}
		if (!member.getGeneration().equals(34)) {  // 기수 갱신 시 조건 변경
			throw new ClientBadRequestException("Only new generation can enroll resolution");
		}
		if (!userResolutionRepository.existsByMember(member)) {
			throw new ClientBadRequestException("Already exist user resolution message");
		}
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
