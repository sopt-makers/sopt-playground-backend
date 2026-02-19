package org.sopt.makers.internal.external.platform;

public record SoptActivity(
        int activityId,
        int generation,
        String part,
        String team,
        String role,
        boolean isSopt
) {
    /**
     * 메이커스 기수를 SOPT 기수로 정규화하여 정렬에 사용
     * 메이커스 1기 = SOPT 31기, 메이커스 2기 = SOPT 32기, 메이커스 3기 = SOPT 33기, 메이커스 4기 = SOPT 34기
     * 메이커스 35기 이상은 SOPT와 동일한 기수 번호 사용
     */
    public int normalizedGeneration() {
        if (!isSopt && generation >= 1 && generation <= 4) {
            return generation + 30;
        }
        return generation;
    }
}