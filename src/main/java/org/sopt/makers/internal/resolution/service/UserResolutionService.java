package org.sopt.makers.internal.resolution.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.PlatformService;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.repository.MemberRepository;
import org.sopt.makers.internal.resolution.domain.ResolutionTag;
import org.sopt.makers.internal.resolution.domain.UserResolution;
import org.sopt.makers.internal.resolution.dto.request.ResolutionSaveRequest;
import org.sopt.makers.internal.resolution.dto.response.ResolutionResponse;
import org.sopt.makers.internal.resolution.dto.response.ResolutionValidResponse;
import org.sopt.makers.internal.resolution.mapper.UserResolutionResponseMapper;
import org.sopt.makers.internal.resolution.repository.UserResolutionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.sopt.makers.internal.common.Constant.*;

@Service
@RequiredArgsConstructor
public class UserResolutionService {

	private final UserResolutionRepository userResolutionRepository;
	private final MemberRepository memberRepository;

	private final UserResolutionResponseMapper userResolutionResponseMapper;
	private final UserResolutionServiceUtil userResolutionServiceUtil;

	private final PlatformService platformService;

	@Transactional(readOnly = true)
	public ResolutionResponse getResolution(Long memberId) {
		InternalUserDetails userDetails = platformService.getInternalUser(memberId);

		Member member = getMemberById(memberId);
		Optional<UserResolution> resolutionOptional = userResolutionRepository.findUserResolutionByMemberAndGeneration(member, CURRENT_GENERATION);

		if (resolutionOptional.isEmpty()){
			return userResolutionResponseMapper.toResolutionResponse(userDetails, null, null);
		}

		UserResolution resolution = resolutionOptional.get();

		return userResolutionResponseMapper.toResolutionResponse(userDetails, resolution.getResolutionTags(), resolution.getContent());
	}

	@Transactional(readOnly = true)
	public ResolutionValidResponse validation(Long memberId) {
		Member member = getMemberById(memberId);
		return userResolutionResponseMapper.toResolutionValidResponse(existsCurrentResolution(member));
	}

	@Transactional
	public void createResolution(Long writerId, ResolutionSaveRequest request) {
		Member member = getMemberById(writerId);
		validateMemberHasGeneration(member);
		validateGeneration(member);
		validateExistingResolution(member);

		UserResolution resolution = userResolutionRepository.save(request.toDomain(member, CURRENT_GENERATION));
        writeToGoogleSheets(writerId, resolution);
	}

	@Transactional
	public void deleteResolution(Long memberId) {
		Member member = getMemberById(memberId);
		validateMemberHasGeneration(member);
		validateGeneration(member);

		UserResolution resolution = userResolutionRepository.findUserResolutionByMemberAndGeneration(member, CURRENT_GENERATION)
				.orElseThrow(() -> new NotFoundDBEntityException("Not exists resolution message"));

		userResolutionRepository.delete(resolution);
	}

	private void validateMemberHasGeneration(Member member) {
		if (member.getGeneration() == null) {
			throw new ClientBadRequestException("Not exists profile info");
		}
	}

	private void validateGeneration(Member member) {
		if (!member.getGeneration().equals(CURRENT_GENERATION)) {
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

    private void writeToGoogleSheets(Long writerId, UserResolution resolution) {
        List<Object> rowData = List.of(
                writerId,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                resolution.getResolutionTags().stream()
                        .map(ResolutionTag::getDescription)
                        .collect(Collectors.joining(", ")),
                resolution.getContent()
        );

		userResolutionServiceUtil.safeWriteToSheets(writerId, List.of(rowData));
    }
}
