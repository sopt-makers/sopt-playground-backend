package org.sopt.makers.internal.member.dto.profile;

import java.util.List;

/**
 * 링크·커리어 집계.
 *
 * <p>정렬 가중치는 <b>개수</b>만 필요하므로 컬렉션 엔티티를 로드하지 않고 집계 쿼리로 가져온다.
 * companyNames 는 검색어 매칭에만 쓰인다.
 *
 * <p>careerCount 와 companyNames.size() 는 다를 수 있다.
 * company_name 이 NULL 인 커리어도 가중치에는 포함되지만 검색 대상은 아니기 때문이다.
 */
public record MemberActivityCountVo(
	int linkCount,
	int careerCount,
	List<String> companyNames
) {

	public static final MemberActivityCountVo EMPTY = new MemberActivityCountVo(0, 0, List.of());

	public MemberActivityCountVo {
		companyNames = (companyNames == null) ? List.of() : List.copyOf(companyNames);
	}

	public boolean matchesCompany(String keyword) {
		return companyNames.stream().anyMatch(companyName -> companyName.contains(keyword));
	}
}
