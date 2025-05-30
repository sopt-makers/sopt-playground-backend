package org.sopt.makers.internal.coffeechat.service;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousProfileImage;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.coffeechat.domain.CoffeeChat;
import org.sopt.makers.internal.coffeechat.dto.request.CoffeeChatDetailsRequest;
import org.sopt.makers.internal.coffeechat.domain.CoffeeChatHistory;
import org.sopt.makers.internal.coffeechat.domain.CoffeeChatReview;
import org.sopt.makers.internal.coffeechat.repository.CoffeeChatHistoryRepository;
import org.sopt.makers.internal.coffeechat.repository.CoffeeChatRepository;
import org.sopt.makers.internal.coffeechat.repository.CoffeeChatReviewRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CoffeeChatModifier {

    private final CoffeeChatRepository coffeeChatRepository;
    private final CoffeeChatHistoryRepository coffeeChatHistoryRepository;
    private final CoffeeChatReviewRepository coffeeChatReviewRepository;

    // CREATE

    public void createCoffeeChatDetails(Member member, CoffeeChatDetailsRequest request) {

        coffeeChatRepository.save(CoffeeChat.builder()
                .member(member)
                .career(request.memberInfo().career())
                .introduction(request.memberInfo().introduction())
                .section(request.coffeeChatInfo().sections())
                .coffeeChatBio(request.coffeeChatInfo().bio())
                .coffeeChatTopicType(request.coffeeChatInfo().topicTypes())
                .topic(request.coffeeChatInfo().topic())
                .meetingType(request.coffeeChatInfo().meetingType())
                .guideline(request.coffeeChatInfo().guideline()).build()
        );
    }

    public void createCoffeeChatHistory(Member sender, Member receiver, String content) {

        coffeeChatHistoryRepository.save(CoffeeChatHistory.builder()
                .sender(sender)
                .receiver(receiver)
                .requestContent(content)
                .build()
        );
    }

    public void createCoffeeChatReview(Member reviewer, CoffeeChat coffeeChat, AnonymousProfileImage anonymousProfileImage, String nickname, String content) {

        coffeeChatReviewRepository.save(CoffeeChatReview.builder()
                .reviewer(reviewer)
                .coffeeChat(coffeeChat)
                .anonymousProfileImage(anonymousProfileImage)
                .nickname(nickname)
                .content(content)
                .build()
        );
    }

    // UPDATE

    public void updateCoffeeChatActivate(CoffeeChat coffeeChat, Boolean isCoffeeChatActivate) {
        coffeeChat.updateCoffeeChatActivate(isCoffeeChatActivate);
    }


    // DELETE

    public void deleteCoffeeChatDetails(CoffeeChat coffeeChat) {

        coffeeChatRepository.delete(coffeeChat);
    }
}
