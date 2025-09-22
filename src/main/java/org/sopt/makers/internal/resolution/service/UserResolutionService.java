package org.sopt.makers.internal.resolution.service;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.PlatformService;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.repository.MemberRepository;
import org.sopt.makers.internal.resolution.domain.UserResolution;
import org.sopt.makers.internal.resolution.dto.request.ResolutionSaveRequest;
import org.sopt.makers.internal.resolution.dto.response.ResolutionResponse;
import org.sopt.makers.internal.resolution.dto.response.ResolutionValidResponse;
import org.sopt.makers.internal.resolution.mapper.UserResolutionResponseMapper;
import org.sopt.makers.internal.resolution.repository.UserResolutionLuckyPickRepository;
import org.sopt.makers.internal.resolution.repository.UserResolutionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.sopt.makers.internal.common.Constant.*;

@Service
@RequiredArgsConstructor
public class UserResolutionService {

	private final UserResolutionRepository userResolutionRepository;
	private final UserResolutionLuckyPickRepository userResolutionLuckyPick;
	private final MemberRepository memberRepository;

	private final UserResolutionResponseMapper userResolutionResponseMapper;

	private final PlatformService platformService;

	@Transactional(readOnly = true)
	public ResolutionResponse getResolution(Long memberId) {
		InternalUserDetails userDetails = platformService.getInternalUser(memberId);

		Member member = getMemberById(memberId);
		return userResolutionRepository.findUserResolutionByMemberAndGeneration(member, CURRENT_GENERATION)
				.map(r -> userResolutionResponseMapper.toResolutionResponse(
						true, r.getResolutionTags(), r.getContent(), hasDrawnTimeCapsule(memberId)))
				.orElseGet(() -> userResolutionResponseMapper.toResolutionResponse(
						false, null, null, hasDrawnTimeCapsule(memberId)));
	}

	private boolean hasDrawnTimeCapsule(Long memberId) {
		return userResolutionLuckyPick.existsByMemberIdAndHasDrawnTrue(memberId);
	}

	@Transactional(readOnly = true)
	public ResolutionValidResponse validation(Long memberId) {
		Member member = getMemberById(memberId);
		return userResolutionResponseMapper.toResolutionValidResponse(existsCurrentResolution(member));
	}

	@Transactional
	public void createResolution(Long writerId, ResolutionSaveRequest request) {
		Member member = getMemberById(writerId);
		InternalUserDetails userDetails = platformService.getInternalUser(writerId);
		validateMemberHasActivities(userDetails);
		validateGeneration(userDetails);
		validateExistingResolution(member);

		userResolutionRepository.save(request.toDomain(member, CURRENT_GENERATION));
	}

	@Transactional
	public void deleteResolution(Long memberId) {
		Member member = getMemberById(memberId);
		InternalUserDetails userDetails = platformService.getInternalUser(memberId);
		validateMemberHasActivities(userDetails);
		validateGeneration(userDetails);

		UserResolution resolution = userResolutionRepository.findUserResolutionByMemberAndGeneration(member, CURRENT_GENERATION)
				.orElseThrow(() -> new NotFoundDBEntityException("Not exists resolution message"));

		userResolutionRepository.delete(resolution);
	}

	private void validateMemberHasActivities(InternalUserDetails userDetails) {
		if (userDetails.soptActivities() == null) {
			throw new ClientBadRequestException("Not exists sopt activities");
		}
	}

	private void validateGeneration(InternalUserDetails userDetails) {
		if (userDetails.lastGeneration() != CURRENT_GENERATION) {
			throw new ClientBadRequestException("Only new generation can enroll resolution");
		}
	}

	private void validateExistingResolution(Member member) {
		if (existsCurrentResolution(member)) {
			throw new ClientBadRequestException("Already exist user resolution message");
		}
	}

	private boolean existsCurrentResolution(Member member) {
		return userResolutionRepository.existsByMemberAndGeneration(member, CURRENT_GENERATION);
	}

	private Member getMemberById(Long userId) {
		return memberRepository.findById(userId).orElseThrow(
			() -> new NotFoundDBEntityException("Is not a Member"));
	}
}
