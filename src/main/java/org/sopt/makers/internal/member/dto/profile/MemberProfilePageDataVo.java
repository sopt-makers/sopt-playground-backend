package org.sopt.makers.internal.member.dto.profile;

import java.util.Map;
import java.util.Set;

import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.dto.response.MemberProfileResponse;

/**
 * 프로필 목록 조회에서 <b>페이지 대상(기본 30건)</b>의 응답 매핑에 필요한 DB 자료 묶음.
 *
 * <p>한 번의 readOnly 트랜잭션 안에서 모두 채워진다. 트랜잭션을 짧게 끊기 위해 매핑은
 * 이 묶음을 받은 뒤 트랜잭션 밖에서 수행하므로, {@code membersById} 의 {@code Member} 는
 * <b>준영속(detached) 상태</b>다. 매핑에 필요한 {@code links} / {@code careers} 는
 * 트랜잭션 안에서 fetch join 으로 이미 초기화되어 있어 세션이 닫힌 뒤에도 읽을 수 있다.
 *
 * <p>질문 미리보기는 {@code MemberQuestion} 엔티티가 아니라 응답 DTO 로 담는다.
 * 엔티티를 그대로 넘기면 트랜잭션 밖에서 LAZY 인 {@code receiver} 를 건드릴 위험이 있다.
 */
public record MemberProfilePageDataVo(
	Map<Long, Member> membersById,
	Map<Long, MemberProfileResponse.MemberQuestionPreviewResponse> questionPreviewsByReceiverId,
	Set<Long> coffeeChatActivatedMemberIds
) {
}
