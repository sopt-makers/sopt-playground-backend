package org.sopt.makers.internal.coffeechat.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousProfileImage;
import org.sopt.makers.internal.member.domain.Member;

import jakarta.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CoffeeChatReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private Member reviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coffee_chat_id", nullable = false)
    private CoffeeChat coffeeChat;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anonymous_profile_image", nullable = false)
    private AnonymousProfileImage anonymousProfileImage;

    @Column(nullable = false, length = 10)
    String nickname;

    @Column(nullable = false, length = 500)
    String content;

    @Builder
    private CoffeeChatReview(Member reviewer, CoffeeChat coffeeChat, AnonymousProfileImage anonymousProfileImage, String nickname, String content) {
        this.reviewer = reviewer;
        this.coffeeChat = coffeeChat;
        this.anonymousProfileImage = anonymousProfileImage;
        this.nickname = nickname;
        this.content = content;
    }
}
