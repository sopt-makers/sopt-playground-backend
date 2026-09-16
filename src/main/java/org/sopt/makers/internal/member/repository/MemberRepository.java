package org.sopt.makers.internal.member.repository;

import java.util.List;
import org.sopt.makers.internal.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {

//    List<Member> findAllByNameContaining(String name);

    List<Member> findAllByIdIn(List<Long> ids);

    List<Member> findAllByHasProfileTrueAndIdIn(List<Long> memberIds);

    @Query("SELECT DISTINCT m FROM Member m LEFT JOIN FETCH m.careers WHERE m.hasProfile = true AND m.id IN :memberIds")
    List<Member> findAllByHasProfileTrueAndIdInWithCareers(@Param("memberIds") List<Long> memberIds);

    /**
     * 응답 매핑에 필요한 Member 를 컬렉션까지 초기화해 로드한다. (페이지 대상에만 사용)
     *
     * <p>links 와 careers 는 둘 다 List(bag) 이라 한 쿼리에서 동시에 fetch join 하면
     * MultipleBagFetchException 이 발생한다. 그래서 두 번에 나눠 조회하며,
     * <b>반드시 같은 트랜잭션(= 같은 영속성 컨텍스트) 안에서</b> 두 메서드를 함께 호출해야
     * 두 번째 쿼리가 첫 번째와 동일한 인스턴스에 나머지 컬렉션을 채운다.
     *
     * <p>DISTINCT 를 쓰지 않는 이유: Hibernate 6 부터 컬렉션 fetch join 의 부모 중복 제거가
     * 결과 리스트에서 자동으로 이뤄진다. DISTINCT 를 쓰면 SQL 로 그대로 내려가 DB 에만
     * 불필요한 중복 제거 비용이 생긴다.
     */
    @Query("SELECT m FROM Member m LEFT JOIN FETCH m.careers WHERE m.id IN :memberIds")
    List<Member> findAllByIdInWithCareers(@Param("memberIds") List<Long> memberIds);

    /** {@link #findAllByIdInWithCareers} 와 짝을 이룬다. 같은 트랜잭션 안에서 호출할 것. */
    @Query("SELECT m FROM Member m LEFT JOIN FETCH m.links WHERE m.id IN :memberIds")
    List<Member> findAllByIdInWithLinks(@Param("memberIds") List<Long> memberIds);

    List<Member> findAllByWorkPreferenceNotNull();

    List<Member> findAllByHasProfileTrue();

    List<Member> findAllByMbtiAndHasProfileTrue(String mbti);

    List<Member> findAllByUniversityAndHasProfileTrue(String university);
}
