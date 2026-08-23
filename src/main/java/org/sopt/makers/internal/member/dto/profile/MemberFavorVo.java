package org.sopt.makers.internal.member.dto.profile;

/**
 * 음식 취향 6종. 엔티티의 {@code UserFavor} 임베디드 타입과 대응한다.
 *
 * <p>엔티티에서는 모든 컬럼이 NULL 이면 Hibernate 가 {@code UserFavor} 자체를 null 로 준다.
 * Projection 에서는 개별 Boolean 이 각각 null 로 오는데, {@link #answeredCount()} 가
 * null 이 아닌 항목만 세므로 6개가 전부 null 이면 0 을 반환한다.
 * 즉 기존의 "favor 가 null 이면 6개 전부 스킵" 과 결과가 같다.
 */
public record MemberFavorVo(
	Boolean isPourSauceLover,
	Boolean isHardPeachLover,
	Boolean isMintChocoLover,
	Boolean isRedBeanFishBreadLover,
	Boolean isSojuLover,
	Boolean isRiceTteokLover
) {

	/** 응답한(= null 이 아닌) 항목 수. */
	public int answeredCount() {
		return ProfileFieldSupport.countNonNull(
			isPourSauceLover, isHardPeachLover, isMintChocoLover,
			isRedBeanFishBreadLover, isSojuLover, isRiceTteokLover
		);
	}
}
