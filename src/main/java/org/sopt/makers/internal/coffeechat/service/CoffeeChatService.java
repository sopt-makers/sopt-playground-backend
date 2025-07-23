package org.sopt.makers.internal.coffeechat.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.makers.internal.coffeechat.domain.CoffeeChat;
import org.sopt.makers.internal.coffeechat.domain.CoffeeChatReview;
import org.sopt.makers.internal.coffeechat.domain.enums.Career;
import org.sopt.makers.internal.coffeechat.domain.enums.ChatCategory;
import org.sopt.makers.internal.coffeechat.domain.enums.CoffeeChatSection;
import org.sopt.makers.internal.coffeechat.domain.enums.CoffeeChatTopicType;
import org.sopt.makers.internal.coffeechat.dto.request.CoffeeChatDetailsRequest;
import org.sopt.makers.internal.coffeechat.dto.request.CoffeeChatInfoDto;
import org.sopt.makers.internal.coffeechat.dto.request.CoffeeChatOpenRequest;
import org.sopt.makers.internal.coffeechat.dto.request.CoffeeChatRequest;
import org.sopt.makers.internal.coffeechat.dto.request.CoffeeChatReviewRequest;
import org.sopt.makers.internal.coffeechat.dto.request.RecentCoffeeChatInfoDto;
import org.sopt.makers.internal.coffeechat.dto.response.CoffeeChatDetailResponse;
import org.sopt.makers.internal.coffeechat.dto.response.CoffeeChatHistoryResponse;
import org.sopt.makers.internal.coffeechat.dto.response.CoffeeChatResponse.CoffeeChatVo;
import org.sopt.makers.internal.coffeechat.dto.response.CoffeeChatReviewResponse.CoffeeChatReviewInfo;
import org.sopt.makers.internal.coffeechat.dto.response.CoffeeChatUserHistoryResponse;
import org.sopt.makers.internal.coffeechat.mapper.CoffeeChatResponseMapper;
import org.sopt.makers.internal.coffeechat.repository.CoffeeChatRepository;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousProfileImage;
import org.sopt.makers.internal.community.service.anonymous.AnonymousProfileImageRetriever;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.external.message.MessageSender;
import org.sopt.makers.internal.external.message.MessageSenderFactory;
import org.sopt.makers.internal.external.message.email.EmailHistoryService;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.MemberSimpleResonse;
import org.sopt.makers.internal.external.platform.PlatformService;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.MemberCareer;
import org.sopt.makers.internal.member.service.MemberRetriever;
import org.sopt.makers.internal.member.service.career.MemberCareerRetriever;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class CoffeeChatService {
    private final MessageSenderFactory messageSenderFactory;

    private final MemberRetriever memberRetriever;
    private final MemberCareerRetriever memberCareerRetriever;

    private final EmailHistoryService emailHistoryService;
    private final PlatformService platformService;

    private final CoffeeChatModifier coffeeChatModifier;
    private final CoffeeChatRetriever coffeeChatRetriever;
    private final CoffeeChatRepository coffeeChatRepository;

    private final AnonymousProfileImageRetriever anonymousProfileImageRetriever;

    private final CoffeeChatResponseMapper coffeeChatResponseMapper;

    @Transactional
    public void sendCoffeeChatRequest(CoffeeChatRequest request, Long senderId) {
        InternalUserDetails senderUserDetails = platformService.getInternalUser(senderId);
        InternalUserDetails receiverUserDetails = platformService.getInternalUser(senderId);
        String replyInfo = getReplyInfo(request, senderUserDetails);

        MessageSender senderStrategy = messageSenderFactory.getSender(request.senderEmail(), request.senderPhone());
        senderStrategy.sendMessage(senderUserDetails, receiverUserDetails, request.content(), replyInfo, request.category());

        createHistoryByCategory(request, senderId, senderUserDetails.email());
    }

    private String getReplyInfo(CoffeeChatRequest request, InternalUserDetails sender) {
        return request.category().equals(ChatCategory.COFFEE_CHAT)
                ? applyDefaultPhone(request.senderPhone(), sender.phone())
                : applyDefaultEmail(request.senderEmail(), sender.email());
    }

    private void createHistoryByCategory(CoffeeChatRequest request, Long senderId, String senderEmail) {
        Member receiver = memberRetriever.findMemberById(request.receiverId());
        Member sender = memberRetriever.findMemberById(senderId);

        if (request.category().equals(ChatCategory.COFFEE_CHAT)) {
            coffeeChatModifier.createCoffeeChatHistory(sender, receiver, request.content());
        } else {
            emailHistoryService.createEmailHistory(request, sender, senderEmail);
        }
    }

    @Transactional(readOnly = true)
    public CoffeeChatDetailResponse getCoffeeChatDetail(Long memberId, Long detailMemberId) {
        memberRetriever.checkExistsMemberById(memberId);

        Member member = memberRetriever.findMemberById(detailMemberId);
        CoffeeChat coffeeChat = coffeeChatRetriever.findCoffeeChatAndCheckIsActivated(member, memberId.equals(detailMemberId));
        MemberCareer memberCareer = memberCareerRetriever.findMemberLastCareerByMemberId(detailMemberId);
        Boolean isMine = Objects.equals(memberId, detailMemberId);
        InternalUserDetails userDetails = platformService.getInternalUser(detailMemberId);
        return coffeeChatResponseMapper.toCoffeeChatDetailResponse(coffeeChat, member, userDetails, memberCareer, isMine);
    }

    @Transactional(readOnly = true)
    public Boolean getCoffeeChatActivate(Long memberId) {
        Member member = memberRetriever.findMemberById(memberId);
        try {
            CoffeeChat coffeeChat = coffeeChatRetriever.findCoffeeChatByMember(member);
            return coffeeChat.getIsCoffeeChatActivate();
        } catch (NotFoundDBEntityException ex) {
            return false;
        }
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
            List<String> soptActivities = platformService.getPartAndGenerationList(coffeeChatInfo.memberId());
            MemberSimpleResonse memberSimpleResonse = platformService.getMemberSimpleInfo(coffeeChatInfo.memberId());
            return coffeeChatResponseMapper.toRecentCoffeeChatResponse(coffeeChatInfo, memberCareer, soptActivities, memberSimpleResonse);
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<CoffeeChatVo> getSearchCoffeeChatList(Long memberId, String section, String topicType, String career, String part, String search) {
        CoffeeChatSection coffeeChatSection = section != null ? CoffeeChatSection.fromTitle(section) : null;
        CoffeeChatTopicType coffeeChatTopicType = topicType != null ? CoffeeChatTopicType.fromTitle(topicType) : null;
        Career coffeeChatCareer = career != null ? Career.fromTitle(career) : null;

        List<CoffeeChatInfoDto> searchCoffeeChatInfo = getSearchCoffeeChatInfoList(memberId, coffeeChatSection, coffeeChatTopicType, coffeeChatCareer, part, search);

        return searchCoffeeChatInfo.stream().map(coffeeChatInfo -> {
            MemberCareer memberCareer = memberCareerRetriever.findMemberLastCareerByMemberId(coffeeChatInfo.memberId());
            List<String> soptActivities = platformService.getPartAndGenerationList(coffeeChatInfo.memberId());
            MemberSimpleResonse memberSimpleResonse = platformService.getMemberSimpleInfo(coffeeChatInfo.memberId());
            return coffeeChatResponseMapper.toCoffeeChatResponse(coffeeChatInfo, memberCareer, soptActivities, memberSimpleResonse);
        }).toList();
    }

    private List<CoffeeChatInfoDto> getSearchCoffeeChatInfoList(Long memberId, CoffeeChatSection section, CoffeeChatTopicType topicType, Career career, String part, String search) {
        List<CoffeeChatInfoDto> coffeeChatInfoList = coffeeChatRepository.findSearchCoffeeChatInfo(memberId, section, topicType, career, search);
        List<CoffeeChatInfoDto> response = coffeeChatInfoList;
        System.out.println(response.size());
        List<Long> userIds = coffeeChatInfoList.stream().map(CoffeeChatInfoDto::memberId).filter(Objects::nonNull).distinct().toList();
        System.out.println(userIds);
        System.out.println(search);

        Map<Long, InternalUserDetails> userMap = getUserMapFromUserIds(userIds);
        if (part != null) { // part 검색은 플랫폼팀에서 받아온 정보로 필터링
            response = response.stream()
                    .filter(dto -> {
                        InternalUserDetails userDetails = userMap.get(dto.memberId());
                        return userDetails.soptActivities().stream()
                                .anyMatch(activity -> part.equalsIgnoreCase(activity.part()));
                    })
                    .toList();
        }

        // TODO - response 사용 안하도록 로직 수정하기 (name 검색은 플랫폼팀에서 받아온 정보로 필터링)
        if (search != null && !search.isBlank()) {
            response = response.stream()
                    .filter(dto -> {
                        InternalUserDetails userDetails = userMap.get(dto.memberId());
                        System.out.println(userDetails.name());
                        return userDetails.name().contains(search);
                    }).toList();
        }

        return response;
    }

    @Transactional(readOnly = true)
    public List<CoffeeChatUserHistoryResponse> getCoffeeChatHistories(Long memberId) {
        List<CoffeeChatHistoryResponse> response = coffeeChatRetriever.getCoffeeChatHistoryTitles(memberId);

        List<Long> userIds = response.stream().map(CoffeeChatHistoryResponse::memberId).toList();
        Map<Long, InternalUserDetails> userDetailsMap = platformService.getInternalUsers(userIds).stream()
                .collect(Collectors.toMap(InternalUserDetails::userId, Function.identity()));

        return response.stream().map(r -> {
            InternalUserDetails userDetails = userDetailsMap.get(r.memberId());
            return new CoffeeChatUserHistoryResponse(r.id(), r.coffeeChatBio(), userDetails.name(), r.career(), r.coffeeChatTopicType());
        }).toList();
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
    public void createCoffeeChatDetails(Long memberId, CoffeeChatDetailsRequest request) {
        Member member = memberRetriever.findMemberById(memberId);

        coffeeChatRetriever.checkAlreadyExistCoffeeChat(member);
        coffeeChatModifier.createCoffeeChatDetails(member, request);
    }

    @Transactional
    public void updateCoffeeChatDetails(Long memberId, CoffeeChatDetailsRequest request) {
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
    public void deleteCoffeeChatDetails(Long memberId) {
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

        AnonymousProfileImage image = anonymousProfileImageRetriever.getAnonymousProfileImage();
        coffeeChatModifier.createCoffeeChatReview(member, coffeeChat, image, request.nickname(), request.content());
    }

    public Map<Long, InternalUserDetails> getUserMapFromUserIds(List<Long> userIds) {
        List<InternalUserDetails> usersDetails = platformService.getInternalUsers(userIds);
        return usersDetails.stream()
                .collect(Collectors.toMap(InternalUserDetails::userId, Function.identity()));
    }

    @Transactional(readOnly = true)
    public List<CoffeeChatReviewInfo> getRecentCoffeeChatReviews() {
        List<CoffeeChatReview> reviews = coffeeChatRetriever.getRecentSixCoffeeChatReviews();

        return reviews.stream().map(review -> {
            CoffeeChat coffeeChat = review.getCoffeeChat();
            return new CoffeeChatReviewInfo(
                    review.getAnonymousProfileImage().getImageUrl(),
                    review.getNickname(),
                    platformService.getPartAndGenerationList(review.getReviewer().getId()),
                    coffeeChat.getCoffeeChatTopicType(),
                    review.getContent()
            );
        }).toList();
    }
}
