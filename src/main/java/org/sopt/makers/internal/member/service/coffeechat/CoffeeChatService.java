package org.sopt.makers.internal.member.service.coffeechat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousProfileImage;
import org.sopt.makers.internal.community.service.anonymous.AnonymousProfileImageRetriever;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.MemberCareer;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.external.MessageSender;
import org.sopt.makers.internal.external.MessageSenderFactory;
import org.sopt.makers.internal.member.controller.coffeechat.dto.request.CoffeeChatReviewRequest;
import org.sopt.makers.internal.member.controller.coffeechat.dto.response.CoffeeChatDetailResponse;
import org.sopt.makers.internal.member.controller.coffeechat.dto.response.CoffeeChatHistoryTitleResponse.CoffeeChatHistoryResponse;
import org.sopt.makers.internal.member.controller.coffeechat.dto.response.CoffeeChatResponse.CoffeeChatVo;
import org.sopt.makers.internal.member.domain.coffeechat.*;
import org.sopt.makers.internal.member.controller.coffeechat.dto.request.CoffeeChatDetailsRequest;
import org.sopt.makers.internal.member.controller.coffeechat.dto.request.CoffeeChatRequest;
import org.sopt.makers.internal.member.controller.coffeechat.dto.request.CoffeeChatOpenRequest;
import org.sopt.makers.internal.member.mapper.coffeechat.CoffeeChatResponseMapper;
import org.sopt.makers.internal.member.repository.coffeechat.dto.CoffeeChatInfoDto;
import org.sopt.makers.internal.member.repository.coffeechat.dto.RecentCoffeeChatInfoDto;
import org.sopt.makers.internal.member.service.MemberRetriever;
import org.sopt.makers.internal.member.service.career.MemberCareerRetriever;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class CoffeeChatService {
    private final MessageSenderFactory messageSenderFactory;

    private final MemberRetriever memberRetriever;
    private final MemberCareerRetriever memberCareerRetriever;

    private final EmailHistoryService emailHistoryService;

    private final CoffeeChatModifier coffeeChatModifier;
    private final CoffeeChatRetriever coffeeChatRetriever;

    private final AnonymousProfileImageRetriever anonymousProfileImageRetriever;

    private final CoffeeChatResponseMapper coffeeChatResponseMapper;

    @Transactional
    public void sendCoffeeChatRequest (CoffeeChatRequest request, Long senderId) {
        Member receiver  = memberRetriever.findMemberById(request.receiverId());
        Member sender = memberRetriever.findMemberById(senderId);

        String replyInfo = getReplyInfo(request, sender);

        MessageSender senderStrategy = messageSenderFactory.getSender(request.senderEmail(), request.senderPhone());
        senderStrategy.sendMessage(sender, receiver, request.content(), replyInfo, request.category());

        createHistoryByCategory(request, sender, receiver);
    }

    private String getReplyInfo(CoffeeChatRequest request, Member sender) {
        return request.category().equals(ChatCategory.COFFEE_CHAT)
                ? applyDefaultPhone(request.senderPhone(), sender.getPhone())
                : applyDefaultEmail(request.senderEmail(), sender.getEmail());
    }

    private void createHistoryByCategory(CoffeeChatRequest request, Member sender, Member receiver) {
        if (request.category().equals(ChatCategory.COFFEE_CHAT)) {
            coffeeChatModifier.createCoffeeChatHistory(sender, receiver, request.content());
        } else {
            emailHistoryService.createEmailHistory(request, sender, sender.getEmail());
        }
    }

    @Transactional(readOnly = true)
    public CoffeeChatDetailResponse getCoffeeChatDetail (Long memberId, Long detailMemberId) {
        memberRetriever.checkExistsMemberById(memberId);

        Member member = memberRetriever.findMemberById(detailMemberId);
        CoffeeChat coffeeChat = coffeeChatRetriever.findCoffeeChatAndCheckIsActivated(member, memberId.equals(detailMemberId));
        MemberCareer memberCareer = memberCareerRetriever.findMemberLastCareerByMemberId(detailMemberId);
        Boolean isMine = Objects.equals(memberId, detailMemberId);
        return coffeeChatResponseMapper.toCoffeeChatDetailResponse(coffeeChat, member, memberCareer, isMine);
    }

    @Transactional(readOnly = true)
    public Boolean getCoffeeChatActivate (Long memberId) {
        Member member = memberRetriever.findMemberById(memberId);
        try {
            CoffeeChat coffeeChat = coffeeChatRetriever.findCoffeeChatByMember(member);
            return coffeeChat.getIsCoffeeChatActivate();
        } catch (NotFoundDBEntityException ex) {
            return false;
        }
    }

    @Transactional(readOnly = true)
    public Boolean isCoffeeChatExist (Long memberId) {
        Member member = memberRetriever.findMemberById(memberId);
        return coffeeChatRetriever.existsCoffeeChat(member);
    }

    @Transactional
    public void updateCoffeeChatOpen(Long memberId, CoffeeChatOpenRequest request) {
        Member member = memberRetriever.findMemberById(memberId);
        CoffeeChat coffeeChat = coffeeChatRetriever.findCoffeeChatByMember(member);

        coffeeChatModifier.updateCoffeeChatActivate(coffeeChat, request.open());
    }

    @Transactional(readOnly = true)
    public List<CoffeeChatVo> getRecentCoffeeChatList() {

        List<RecentCoffeeChatInfoDto> recentCoffeeChatInfo = coffeeChatRetriever.recentCoffeeChatInfoList();
        return recentCoffeeChatInfo.stream().map(coffeeChatInfo -> {
            MemberCareer memberCareer = memberCareerRetriever.findMemberLastCareerByMemberId(coffeeChatInfo.memberId());
            List<String> soptActivities = memberRetriever.concatPartAndGeneration(coffeeChatInfo.memberId());
            return coffeeChatResponseMapper.toRecentCoffeeChatResponse(coffeeChatInfo, memberCareer, soptActivities);
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<CoffeeChatVo> getSearchCoffeeChatList (Long memberId, String section, String topicType, String career, String part, String search) {

        CoffeeChatSection coffeeChatSection = section != null ? CoffeeChatSection.fromTitle(section) : null;
        CoffeeChatTopicType coffeeChatTopicType = topicType != null ? CoffeeChatTopicType.fromTitle(topicType) : null;
        Career coffeeChatCareer = career != null ? Career.fromTitle(career) : null;
        List<CoffeeChatInfoDto> searchCoffeeChatInfo = coffeeChatRetriever.searchCoffeeChatInfo(memberId, coffeeChatSection, coffeeChatTopicType, coffeeChatCareer, part, search);
        return searchCoffeeChatInfo.stream().map(coffeeChatInfo -> {
            MemberCareer memberCareer = memberCareerRetriever.findMemberLastCareerByMemberId(coffeeChatInfo.memberId());
            List<String> soptActivities = memberRetriever.concatPartAndGeneration(coffeeChatInfo.memberId());
            return coffeeChatResponseMapper.toCoffeeChatResponse(coffeeChatInfo, memberCareer, soptActivities);
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<CoffeeChatHistoryResponse> getCoffeeChatHistories(Long memberId) {
        return coffeeChatRetriever.getCoffeeChatHistoryTitles(memberId);
    }

    private String applyDefaultEmail(String requestEmail, String senderEmail) {
        if (requestEmail == null) {
            return senderEmail;
        }
        return requestEmail;
    }

    private String applyDefaultPhone(String requestPhone, String senderPhone) {
        if (requestPhone == null) {
            return senderPhone;
        }
        return requestPhone;
    }

    @Transactional
    public void createCoffeeChatDetails (Long memberId, CoffeeChatDetailsRequest request) {
        Member member = memberRetriever.findMemberById(memberId);

        coffeeChatRetriever.checkAlreadyExistCoffeeChat(member);
        coffeeChatModifier.createCoffeeChatDetails(member, request);
    }

    @Transactional
    public void updateCoffeeChatDetails (Long memberId, CoffeeChatDetailsRequest request) {
        Member member = memberRetriever.findMemberById(memberId);

        CoffeeChat coffeeChat = coffeeChatRetriever.findCoffeeChatByMember(member);
        coffeeChat.updateCoffeeChatInfo(
                request.memberInfo().career(),
                request.memberInfo().introduction(),
                request.coffeeChatInfo().sections(),
                request.coffeeChatInfo().bio(),
                request.coffeeChatInfo().topicTypes(),
                request.coffeeChatInfo().topic(),
                request.coffeeChatInfo().meetingType(),
                request.coffeeChatInfo().guideline()
        );
    }

    @Transactional
    public void deleteCoffeeChatDetails (Long memberId) {
        Member member = memberRetriever.findMemberById(memberId);

        CoffeeChat coffeeChat = coffeeChatRetriever.findCoffeeChatByMember(member);
        coffeeChatModifier.deleteCoffeeChatDetails(coffeeChat);
    }

    @Transactional
    public void createCoffeeChatReview(Long memberId, CoffeeChatReviewRequest request) {
        Member member = memberRetriever.findMemberById(memberId);
        CoffeeChat coffeeChat = coffeeChatRetriever.findCoffeeChatById(request.coffeeChatId());
        coffeeChatRetriever.checkParticipateCoffeeChat(member, coffeeChat);
        coffeeChatRetriever.checkAlreadyEnrollReview(member, coffeeChat);
        List<Long> recentUsedAnonymousProfileImageIds = coffeeChatRetriever.getRecentUsedAnonymousProfileImageIdsInCoffeeChatReview();
        AnonymousProfileImage image = anonymousProfileImageRetriever.getAnonymousProfileImage(recentUsedAnonymousProfileImageIds);
        coffeeChatModifier.createCoffeeChatReview(member, coffeeChat, image, request.nickname(), request.content());
    }
}
