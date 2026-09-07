package org.sopt.makers.internal.member.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.coffeechat.service.CoffeeChatRetriever;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.dto.profile.MemberProfilePageDataVo;
import org.sopt.makers.internal.member.dto.profile.MemberProfileSummaryVo;
import org.sopt.makers.internal.member.dto.response.MemberProfileResponse;
import org.sopt.makers.internal.member.repository.MemberProfileQueryRepository;
import org.sopt.makers.internal.member.repository.MemberRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 프로필 목록 조회({@link MemberProfileListService})의 DB 접근 구간 전담.
 *
 * <p><b>이 클래스가 따로 있는 이유는 트랜잭션 경계다.</b> 목록 조회 한 번에는 플랫폼 서버 HTTP
 * 왕복과 전원 정렬(운영 기준 약 1,300건)이 끼어 있는데, 예전처럼 조회 메서드 전체를
 * {@code @Transactional} 로 감싸면 DB 를 전혀 쓰지 않는 그 구간에도 커넥션이 묶인다.
 * {@code @Transactional} 은 프록시 기반이라 같은 클래스 안에서 자기 호출로는 경계를 나눌 수 없어,
 * DB 구간만 별도 빈의 메서드로 떼어냈다. 서비스에는 트랜잭션이 없고 여기에만 있다.
 *
 * <p>다른 {@code XxxRetriever} 와 달리 메서드마다 {@code @Transactional(readOnly = true)} 가
 * 붙어 있는 것은 위 의도 때문이다.
 */
@Component
@RequiredArgsConstructor
public class MemberProfileListRetriever {

	private final MemberRepository memberRepository;
	private final MemberProfileQueryRepository memberProfileQueryRepository;
	private final MemberQuestionRetriever memberQuestionRetriever;
	private final CoffeeChatRetriever coffeeChatRetriever;

	/** DB 필터(hasProfile, mbti, employed)를 통과한 전체 userId. 쿼리 1회. */
	@Transactional(readOnly = true)
	public List<Long> findFilteredMemberIds(String mbti, Integer employed, String search) {
		return memberProfileQueryRepository.findAllMemberIdsByDbFilters(mbti, employed, search);
	}

	/** 검색·정렬에 필요한 값만 담은 경량 Projection. 엔티티를 로드하지 않는다. 쿼리 3회. */
	@Transactional(readOnly = true)
	public Map<Long, MemberProfileSummaryVo> findProfileSummariesByIds(List<Long> memberIds) {
		return memberProfileQueryRepository.findMemberProfileSummariesByIds(memberIds).stream()
			.collect(Collectors.toMap(MemberProfileSummaryVo::id, Function.identity()));
	}

	/**
	 * 페이지 대상의 응답 매핑에 필요한 자료를 <b>한 트랜잭션 안에서</b> 모두 채운다. 쿼리 4회.
	 *
	 * <p>엔티티 로드가 두 쿼리로 나뉘어 있는데, 이 둘은 반드시 같은 영속성 컨텍스트에서 실행돼야 한다.
	 * {@code links} 와 {@code careers} 는 둘 다 List(bag) 이라 한 쿼리에서 동시에 fetch join 할 수 없고
	 * ({@code MultipleBagFetchException}), 같은 트랜잭션이어야 두 번째 쿼리가 첫 번째 쿼리로 이미
	 * 영속화된 인스턴스에 {@code links} 를 채워준다. 쪼개면 {@code links} 가 미초기화 상태로 남아
	 * 매핑 시점에 {@code LazyInitializationException} 이 난다.
	 */
	@Transactional(readOnly = true)
	public MemberProfilePageDataVo loadPageData(List<Long> pagedMemberIds, LocalDateTime questionPreviewSince) {
		List<Member> pagedMembers = memberRepository.findAllByIdInWithCareers(pagedMemberIds);
		memberRepository.findAllByIdInWithLinks(pagedMemberIds);
		Map<Long, Member> membersById = pagedMembers.stream()
			.collect(Collectors.toMap(Member::getId, Function.identity()));

		// 엔티티가 아니라 응답 DTO 로 변환해 내보낸다. 트랜잭션 밖에서 LAZY 인 receiver 를 건드리지 않기 위함.
		Map<Long, MemberProfileResponse.MemberQuestionPreviewResponse> questionPreviewsByReceiverId =
			memberQuestionRetriever.findLatestRecentQuestionsByReceiverIds(pagedMemberIds, questionPreviewSince)
				.stream().collect(Collectors.toMap(
					question -> question.getReceiver().getId(),
					question -> new MemberProfileResponse.MemberQuestionPreviewResponse(
						question.getId(),
						question.getContent()
					)
				));

		// 커피챗 활성 여부도 페이지 인원만큼 물으면 인원수만큼 쿼리가 나가므로 한 번에 조회한다.
		Set<Long> coffeeChatActivatedMemberIds = coffeeChatRetriever.findActivatedMemberIds(pagedMemberIds);

		return new MemberProfilePageDataVo(membersById, questionPreviewsByReceiverId, coffeeChatActivatedMemberIds);
	}
}
