package org.sopt.makers.internal.coffeechat.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.sopt.makers.internal.coffeechat.domain.CoffeeChat;
import org.sopt.makers.internal.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CoffeeChatRepository extends JpaRepository<CoffeeChat, Long>, CoffeeChatRepositoryCustom {

    // CREATE

    // READ
    Boolean existsCoffeeChatByMember(Member member);
    Boolean existsCoffeeChatByMemberAndIsCoffeeChatActivate(Member member, Boolean isCoffeeChatActivate);
    Optional<CoffeeChat> findCoffeeChatByMember(Member member);

    /**
     * 활성화된 커피챗을 가진 멤버 ID 를 한 번에 조회한다.
     *
     * <p>목록 응답을 만들 때 멤버마다 존재 여부를 묻던 것을 벌크 조회로 대체하기 위한 메서드다.
     */
    @Query("SELECT c.member.id FROM CoffeeChat c WHERE c.member.id IN :memberIds AND c.isCoffeeChatActivate = true")
    Set<Long> findActivatedMemberIdsIn(@Param("memberIds") List<Long> memberIds);

    // UPDATE

    // DELETE
}
