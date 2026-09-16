package org.sopt.makers.internal.member.dto.profile;

/**
 * 멤버 프로필 목록 조회에서 <b>어떤 멤버를 고르고 어떤 순서로 놓을지</b> 판단하기 위한
 * 로컬 프로필 요약. 검색 필터링과 정렬 가중치 계산에 사용한다.
 *
 * <p>기존에는 이 판단을 위해 {@code Member} 엔티티를 전량 로드했고, 가중치 계산이
 * LAZY 컬렉션({@code links}, {@code careers})을 건드려 멤버당 2회의 추가 쿼리가 발생했다.
 * 이 VO 는 필요한 값만 담아 그 N+1 을 제거한다.
 *
 * <h2>API 응답에 사용하지 말 것</h2>
 * <p>이 VO 에는 <b>이름도 프로필 이미지도 없다.</b> 둘 다 플랫폼 서버가 소유한 값이라
 * 이 프로젝트 DB 에 존재하지 않는다. 목록 응답을 만들려고 여기에 필드를 덧붙이기 시작하면
 * projection 이 다시 넓어져 위에서 없앤 N+1 이 그대로 되돌아온다.
 *
 * <p>응답 직렬화는 페이지 대상(기본 30건)에 대해서만 엔티티를 fetch join 으로 로드해
 * {@code MemberMapper} 가 담당한다. 그쪽 경로를 사용할 것.
 */
public record MemberProfileSummaryVo(
	Long id,
	MemberBasicInfoVo basicInfo,
	MemberIntroVo intro,
	MemberPersonalityVo personality,
	MemberFavorVo favor,
	MemberActivityCountVo activity
) {

	/**
	 * QueryDSL 프로젝션용 생성자.
	 * 집계값(activity)은 별도 쿼리로 구해 {@link #withActivity} 로 채운다.
	 */
	public MemberProfileSummaryVo(
		Long id,
		MemberBasicInfoVo basicInfo,
		MemberIntroVo intro,
		MemberPersonalityVo personality,
		MemberFavorVo favor
	) {
		this(id, basicInfo, intro, personality, favor, MemberActivityCountVo.EMPTY);
	}

	public MemberProfileSummaryVo withActivity(MemberActivityCountVo activity) {
		return new MemberProfileSummaryVo(id, basicInfo, intro, personality, favor, activity);
	}

	/** 검색어가 대학교명 또는 회사명에 포함되는지. 이름 매칭은 플랫폼 데이터라 호출부가 담당한다. */
	public boolean matchesUniversityOrCompany(String keyword) {
		return basicInfo.matchesUniversity(keyword) || activity.matchesCompany(keyword);
	}
}
