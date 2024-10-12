package org.sopt.makers.internal.member.domain.coffeechat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import org.hibernate.annotations.ColumnDefault;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.common.AuditingTimeEntity;

import javax.persistence.*;


@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CoffeeChat extends AuditingTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault("true")
    private Boolean isCoffeeChatActivate = true;

    @Column(nullable = false, length = 40)
    private String coffeeChatBio;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public void updateCoffeeChatInformation(Boolean isCoffeeChatActivate, String coffeeChatBio) {
        this.isCoffeeChatActivate = isCoffeeChatActivate;
        this.coffeeChatBio = coffeeChatBio;
    }
}
