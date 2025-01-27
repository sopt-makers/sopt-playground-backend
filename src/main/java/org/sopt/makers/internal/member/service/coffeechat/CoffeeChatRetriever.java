package org.sopt.makers.internal.member.service.coffeechat;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.member.controller.coffeechat.dto.response.CoffeeChatHistoryTitleResponse.CoffeeChatHistoryResponse;
import org.sopt.makers.internal.member.domain.coffeechat.*;
import org.sopt.makers.internal.member.repository.coffeechat.CoffeeChatHistoryRepository;
import org.sopt.makers.internal.member.repository.coffeechat.CoffeeChatRepository;
import org.sopt.makers.internal.member.repository.coffeechat.CoffeeChatReviewRepository;
import org.sopt.makers.internal.member.repository.coffeechat.dto.CoffeeChatInfoDto;
import org.sopt.makers.internal.member.repository.coffeechat.dto.RecentCoffeeChatInfoDto;
import org.sopt.makers.internal.member.service.coffeechat.dto.MemberCoffeeChatPropertyDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CoffeeChatRetriever {

    private final CoffeeChatRepository coffeeChatRepository;
    private final CoffeeChatHistoryRepository coffeeChatHistoryRepository;
    private final CoffeeChatReviewRepository coffeeChatReviewRepository;

    public CoffeeChat findCoffeeChatByMember(Member member) {
        return coffeeChatRepository.findCoffeeChatByMember(member)
                .orElseThrow(() -> new NotFoundDBEntityException("커피챗 정보를 등록한적 없는 유저입니다. " + "member id: " + member.getId()));
    }

    public CoffeeChat findCoffeeChatById(Long id) {
        return coffeeChatRepository.findById(id)
                .orElseThrow(() -> new NotFoundDBEntityException("존재하지 않는 커피챗입니다. " + "coffee chat id: " + id));
    }

    public void checkAlreadyExistCoffeeChat(Member member) {
        if (coffeeChatRepository.existsCoffeeChatByMember(member)) {
            throw new ClientBadRequestException("이미 커피챗 정보가 등록된 유저입니다. " + "member id: " + member.getId());
        }
    }

    public boolean existsCoffeeChat(Member member) {
        return coffeeChatRepository.existsCoffeeChatByMember(member);
    }

    public CoffeeChat findCoffeeChatAndCheckIsActivated(Member member, Boolean isMine) {
        return coffeeChatRepository.findCoffeeChatByMember(member)
                .filter(coffeeChat -> coffeeChat.getIsCoffeeChatActivate() || isMine)
                .orElseThrow(() -> new NotFoundDBEntityException("커피챗 정보를 확인할 수 없는 유저입니다. member id: " + member.getId()));
    }

    public List<RecentCoffeeChatInfoDto> recentCoffeeChatInfoList() {
        return coffeeChatRepository.findRecentCoffeeChatInfo();
    }

    public List<CoffeeChatInfoDto> searchCoffeeChatInfo(Long memberId, CoffeeChatSection section, CoffeeChatTopicType topicType, Career career, String part, String search) {
        return coffeeChatRepository.findSearchCoffeeChatInfo(memberId, section, topicType, career, part, search);
    }

    public MemberCoffeeChatPropertyDto getMemberCoffeeChatProperty(Member member) {

        Long receivedCoffeeChatCount = coffeeChatHistoryRepository.countByReceiver(member);
        Long sentCoffeeChatCount = coffeeChatHistoryRepository.countBySender(member);
        CoffeeChatStatus coffeeChatStatus;

        if (!coffeeChatRepository.existsCoffeeChatByMember(member)) {
            coffeeChatStatus = CoffeeChatStatus.NONE;
        } else {
            CoffeeChat coffeeChat = findCoffeeChatByMember(member);
            coffeeChatStatus = coffeeChat.getIsCoffeeChatActivate() ? CoffeeChatStatus.ON : CoffeeChatStatus.OFF;
        }

        return new MemberCoffeeChatPropertyDto(coffeeChatStatus, receivedCoffeeChatCount, sentCoffeeChatCount);
    }

    public List<CoffeeChatHistoryResponse> getCoffeeChatHistoryTitles(Long memberId) {
        return coffeeChatRepository.getCoffeeChatHistoryTitles(memberId);
    }

    public void checkParticipateCoffeeChat(Member member, CoffeeChat coffeeChat) {

        if (!coffeeChatHistoryRepository.existsByReceiverAndSender(coffeeChat.getMember(), member)) {
            throw new ClientBadRequestException("해당 커피챗을 신청한 적 없는 유저입니다. " + "member id: " + member.getId());
        }
    }
    public List<Long> getRecentUsedAnonymousProfileImageIdsInCoffeeChatReview() {

        return coffeeChatReviewRepository.findTop4ByOrderByIdDesc()
                .stream()
                .map(review -> review.getAnonymousProfileImage().getId())
                .collect(Collectors.toList());
    }

    public void checkAlreadyEnrollReview(Member member, CoffeeChat coffeeChat) {

        if (coffeeChatReviewRepository.existsByReviewerAndCoffeeChat(member, coffeeChat)) {
            throw new ClientBadRequestException("이미 리뷰를 등록한 커피챗입니다.");
        }
    }
}