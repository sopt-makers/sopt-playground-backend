package org.sopt.makers.internal.coffeechat.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.makers.internal.coffeechat.domain.CoffeeChat;
import org.sopt.makers.internal.coffeechat.domain.CoffeeChatReview;
import org.sopt.makers.internal.coffeechat.domain.enums.Career;
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
import org.sopt.makers.internal.coffeechat.dto.response.RandomCoffeeChatResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sopt.makers.internal.coffeechat.dto.response.CoffeeChatUserHistoryResponse;
import org.sopt.makers.internal.coffeechat.mapper.CoffeeChatResponseMapper;
import org.sopt.makers.internal.coffeechat.repository.CoffeeChatRepository;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousProfileImage;
import org.sopt.makers.internal.community.service.anonymous.AnonymousProfileImageRetriever;
import org.sopt.makers.internal.exception.NotFoundException;
import org.sopt.makers.internal.external.message.SmsChatSender;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.MemberSimpleResonse;
import org.sopt.makers.internal.external.platform.PlatformService;
import org.sopt.makers.internal.external.platform.SoptActivity;
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

    public static final String RANDOM_COFFEE_CHAT_REDIS_KEY = "coffeeChat:random";
    public static final java.time.Duration RANDOM_COFFEE_CHAT_TTL = java.time.Duration.ofHours(25);

    private final SmsChatSender smsChatSender;

    private final MemberRetriever memberRetriever;
    private final MemberCareerRetriever memberCareerRetriever;

    private final PlatformService platformService;

    private final CoffeeChatModifier coffeeChatModifier;
    private final CoffeeChatRetriever coffeeChatRetriever;
    private final CoffeeChatRepository coffeeChatRepository;
    private final AnonymousProfileImageRetriever anonymousProfileImageRetriever;

    private final ObjectMapper objectMapper;
    private final org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate;

    private final CoffeeChatResponseMapper coffeeChatResponseMapper;

    @Transactional
    public void sendCoffeeChatRequest(CoffeeChatRequest request, Long senderId) {
        InternalUserDetails senderUserDetails = platformService.getInternalUser(senderId);
        InternalUserDetails receiverUserDetails = platformService.getInternalUser(request.receiverId());
        String senderPhone = applyDefaultPhone(request.senderPhone(), senderUserDetails.phone());

        smsChatSender.sendMessage(senderUserDetails, receiverUserDetails, request.content(), senderPhone, request.category());

        createCoffeeChatHistory(request, senderId);
    }

    private void createCoffeeChatHistory(CoffeeChatRequest request, Long senderId) {
        Member receiver = memberRetriever.findMemberById(request.receiverId());
        Member sender = memberRetriever.findMemberById(senderId);

        coffeeChatModifier.createCoffeeChatHistory(sender, receiver, request.content());
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
        } catch (NotFoundException ex) {
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
        List<CoffeeChatInfoDto> response =
            coffeeChatRepository.findCoffeeChatInfoByDbConditions(memberId, career);

        response = response.stream()
            .filter(distinctByKey(CoffeeChatInfoDto::memberId))
            .toList();

        List<Long> userIds = response.stream().map(CoffeeChatInfoDto::memberId).filter(Objects::nonNull).distinct().toList();
        Map<Long, InternalUserDetails> userMap = getUserMapFromUserIds(userIds);

        if (section != null) {
            response = response.stream()
                .filter(dto -> dto.sectionList() != null && dto.sectionList().contains(section))
                .toList();
        }

        if (topicType != null) {
            response = response.stream()
                .filter(dto -> dto.topicTypeList() != null && dto.topicTypeList().contains(topicType))
                .toList();
        }

        if (part != null) {
            response = response.stream()
                .filter(dto -> {
                    InternalUserDetails userDetails = userMap.get(dto.memberId());
                    return userDetails != null && userDetails.soptActivities().stream()
                        .anyMatch(activity -> part.equalsIgnoreCase(activity.part()));
                })
                .toList();
        }

        if (search != null && !search.isBlank()) {
            response = response.stream()
                .filter(dto -> {
                    boolean dbFieldMatch = (dto.university() != null && dto.university().contains(search))
                        || (dto.companyName() != null && dto.companyName().contains(search));

                    InternalUserDetails userDetails = userMap.get(dto.memberId());
                    boolean nameFieldMatch = userDetails != null && userDetails.name().contains(search);

                    return dbFieldMatch || nameFieldMatch;
                })
                .toList();
        }

        return response;
    }

    @Transactional(readOnly = true)
    public List<CoffeeChatUserHistoryResponse> getCoffeeChatHistories(Long memberId) {
        List<CoffeeChatHistoryResponse> response = coffeeChatRetriever.getCoffeeChatHistoryTitles(memberId);

        List<Long> userIds = response.stream().map(CoffeeChatHistoryResponse::memberId).toList();
        Map<Long, InternalUserDetails> userDetailsMap = platformService.getInternalUsers(userIds).stream()
            .collect(Collectors.toMap(
                InternalUserDetails::userId,
                Function.identity(),
                (existing, replacement) -> existing
            ));

        return response.stream().map(r -> {
            InternalUserDetails userDetails = userDetailsMap.get(r.memberId());
            return new CoffeeChatUserHistoryResponse(r.id(), r.coffeeChatBio(), userDetails.name(), r.career(), r.coffeeChatTopicType());
        }).toList();
    }

    private String applyDefaultPhone(String requestPhone, String senderPhone) {
        if (requestPhone == null || requestPhone.isBlank()) {
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
            .collect(Collectors.toMap(
                InternalUserDetails::userId,
                Function.identity(),
                (existing, replacement) -> existing
            ));
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

    public List<RandomCoffeeChatResponse> getRandomCoffeeChatList(Long userId) {
        List<RandomCoffeeChatResponse> pool;
        try {
            Object cached = redisTemplate.opsForValue().get(RANDOM_COFFEE_CHAT_REDIS_KEY);
            if (cached != null) {
                pool = objectMapper.readValue(cached.toString(), new TypeReference<>() {});
            } else {
                pool = buildRandomCoffeeChatList();
                String json = objectMapper.writeValueAsString(pool);
                redisTemplate.opsForValue().set(RANDOM_COFFEE_CHAT_REDIS_KEY, json, RANDOM_COFFEE_CHAT_TTL);
            }
        } catch (Exception e) {
            log.warn("[CoffeeChatService] Redis 캐시 조회 실패, DB fallback 실행", e);
            pool = buildRandomCoffeeChatList();
        }

        InternalUserDetails currentUser = platformService.getInternalUser(userId);
        String userPart = currentUser.soptActivities().stream()
                .max(Comparator.comparing(SoptActivity::normalizedGeneration))
                .map(SoptActivity::part)
                .orElseThrow();

        List<RandomCoffeeChatResponse> samePart = pool.stream()
                .filter(r -> r.soptActivities().stream()
                        .anyMatch(a -> a.endsWith(" " + userPart)))
                .toList();
        List<RandomCoffeeChatResponse> otherPart = pool.stream()
                .filter(r -> r.soptActivities().stream()
                        .noneMatch(a -> a.endsWith(" " + userPart)))
                .toList();

        List<RandomCoffeeChatResponse> result = new ArrayList<>(samePart.subList(0, Math.min(samePart.size(), 4)));
        if (result.size() < 4) {
            result.addAll(otherPart.subList(0, Math.min(otherPart.size(), 4 - result.size())));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<RandomCoffeeChatResponse> buildRandomCoffeeChatList() {
        return coffeeChatRetriever.findRandomActiveCoffeeChats(20).stream()
                .map(coffeeChat -> {
                    Long memberId = coffeeChat.getMember().getId();
                    InternalUserDetails userDetails = platformService.getInternalUser(memberId);
                    MemberCareer memberCareer = memberCareerRetriever.findMemberLastCareerByMemberId(memberId);
                    List<String> soptActivities = userDetails.soptActivities().stream()
                            .sorted(Comparator.comparing(SoptActivity::normalizedGeneration)
                                    .thenComparing(a -> !a.isSopt()))
                            .map(a -> a.isSopt()
                                    ? String.format("%d기 %s", a.generation(), a.part())
                                    : String.format("%d기 메이커스", a.generation()))
                            .toList();
                    return new RandomCoffeeChatResponse(
                            memberId,
                            coffeeChat.getCoffeeChatBio(),
                            userDetails.profileImage(),
                            userDetails.name(),
                            coffeeChat.getCareer().getTitle(),
                            memberCareer != null ? memberCareer.getCompanyName() : coffeeChat.getMember().getUniversity(),
                            memberCareer != null ? memberCareer.getTitle() : null,
                            soptActivities,
                            coffeeChat.getCoffeeChatTopicType().stream().map(CoffeeChatTopicType::getTitle).toList()
                    );
                })
                .toList();
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
}
