package org.sopt.makers.internal.resolution.service;

import static org.sopt.makers.internal.common.Constant.*;

import java.util.List;
import java.util.Optional;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.repository.MemberRepository;
import org.sopt.makers.internal.resolution.domain.ResolutionTag;
import org.sopt.makers.internal.resolution.domain.UserResolution;
import org.sopt.makers.internal.resolution.dto.request.ResolutionSaveRequest;
import org.sopt.makers.internal.resolution.dto.response.ResolutionResponse;
import org.sopt.makers.internal.resolution.dto.response.ResolutionValidResponse;
import org.sopt.makers.internal.resolution.mapper.UserResolutionResponseMapper;
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

	private final UserResolutionResponseMapper userResolutionResponseMapper;

	private final static String DEFAULT_PROFILE_IMAGE = "";

	@Transactional(readOnly = true)
	public ResolutionResponse getResolution(Long memberId) {
		Member member = getMemberById(memberId);
		Optional<UserResolution> resolutionOptional = userResolutionRepository.findUserResolutionByMemberAndGeneration(member, CURRENT_GENERATION);

		if(resolutionOptional.isEmpty()){
			return userResolutionResponseMapper.toResolutionResponse(member, null, null);
		}

		UserResolution resolution = resolutionOptional.get();
		List<String> tags = getTagNamesOrNull(resolution.getTagIds());

		return userResolutionResponseMapper.toResolutionResponse(member, tags, resolution.getContent());
	}

	@Transactional(readOnly = true)
	public ResolutionValidResponse validation(Long memberId) {
		val member = getMemberById(memberId);
		val isRegistration = existsCurrentResolution(member);

		return userResolutionResponseMapper.toResolutionValidResponse(isRegistration);
	}

	@Transactional
	public void createResolution(Long writerId, ResolutionSaveRequest request) {
		val member = getMemberById(writerId);
		if (member.getGeneration() == null) {
			throw new ClientBadRequestException("Not exists profile info");
		}
		if (!member.getGeneration().equals(CURRENT_GENERATION)) {  // 기수 갱신 시 조건 변경
			throw new ClientBadRequestException("Only new generation can enroll resolution");
		}
		if (existsCurrentResolution(member)) {  // TODO 기수마다 1개씩 가능하도록 수정
			throw new ClientBadRequestException("Already exist user resolution message");
		}
		UserResolution userResolution = UserResolution.builder()
			.member(member)
			.tagIds(ResolutionTag.getTagIds(request.tags()))
			.content(request.content())
			.generation(CURRENT_GENERATION).build();
		userResolutionRepository.save(userResolution);
	}

	@Transactional
	public void deleteResolution(Long memberId){
		Member member = validateMember(memberId);
		validateGeneration(member);
		validateNoExistingResolution(member);

		UserResolution resolution = UserResolutionServiceUtil.findUserResolutionByMember(member, userResolutionRepository);

		userResolutionRepository.delete(resolution);
	}

	private Member validateMember(Long memberId){
		Member member = getMemberById(memberId);
		if (member.getGeneration() == null) {
			throw new ClientBadRequestException("Not exists profile info");
		}
		return member;
	}

	private void validateGeneration(Member member){
		if(!member.getGeneration().equals(CURRENT_GENERATION)){
			throw new ClientBadRequestException("Only new generation can enroll resolution");
		}
	}

	private void validateNoExistingResolution(Member member){
		if(!existsCurrentResolution(member)){
			throw new ClientBadRequestException("No exist user resolution message");
		}
	}

	private boolean existsCurrentResolution(Member member) {
		return userResolutionRepository.existsByMemberAndGeneration(member, CURRENT_GENERATION);
	}

	private Member getMemberById(Long userId) {
		return memberRepository.findById(userId).orElseThrow(
			() -> new NotFoundDBEntityException("Is not a Member"));
	}

	private List<String> getTagNamesOrNull(String tagIds) {
		if(tagIds == null || tagIds.isEmpty()) return null;
		return ResolutionTag.getTagNames(tagIds);
	}
}
