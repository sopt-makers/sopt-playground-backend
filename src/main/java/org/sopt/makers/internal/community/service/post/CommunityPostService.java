package org.sopt.makers.internal.community.service.post;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import org.hibernate.Hibernate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.sopt.makers.internal.community.domain.CommunityPost;
import org.sopt.makers.internal.community.domain.CommunityPostLike;
import org.sopt.makers.internal.community.domain.ReportPost;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousProfile;
import org.sopt.makers.internal.community.domain.category.Category;
import org.sopt.makers.internal.community.domain.enums.CommunityCategoryCode;
import org.sopt.makers.internal.community.domain.enums.CommunityCategoryGroup;
import org.sopt.makers.internal.community.domain.enums.CommunityPostListCategory;
import org.sopt.makers.internal.community.domain.enums.CommunityPostListFilter;
import org.sopt.makers.internal.community.domain.enums.CommunityPostSourceType;
import org.sopt.makers.internal.community.dto.CategoryPostMemberDao;
import org.sopt.makers.internal.community.dto.CommunityDbCursor;
import org.sopt.makers.internal.community.dto.CommunityFeedCursor;
import org.sopt.makers.internal.community.dto.CommunityPostMemberVo;
import org.sopt.makers.internal.community.dto.MemberVo;
import org.sopt.makers.internal.community.dto.PostCategoryDao;
import org.sopt.makers.internal.community.dto.PostDetailData;
import org.sopt.makers.internal.community.dto.comment.CommentInfo;
import org.sopt.makers.internal.community.dto.request.MentionRequest;
import org.sopt.makers.internal.community.dto.request.PostSaveRequest;
import org.sopt.makers.internal.community.dto.request.PostUpdateRequest;
import org.sopt.makers.internal.community.dto.response.PopularPostResponse;
import org.sopt.makers.internal.community.dto.response.PostAllResponse;
import org.sopt.makers.internal.community.dto.response.PostResponse;
import org.sopt.makers.internal.community.dto.response.PostSaveResponse;
import org.sopt.makers.internal.community.dto.response.PostUpdateResponse;
import org.sopt.makers.internal.community.dto.response.RecentPostResponse;
import org.sopt.makers.internal.community.dto.response.SopticlePostResponse;
import org.sopt.makers.internal.community.dto.response.SopticleScrapedResponse;
import org.sopt.makers.internal.community.mapper.CommunityMapper;
import org.sopt.makers.internal.community.mapper.CommunityResponseMapper;
import org.sopt.makers.internal.community.repository.CommunityQueryRepository;
import org.sopt.makers.internal.community.repository.ReportPostRepository;
import org.sopt.makers.internal.community.service.anonymous.AnonymousProfileRetriever;
import org.sopt.makers.internal.community.repository.comment.CommunityCommentRepository;
import org.sopt.makers.internal.community.repository.comment.DeletedCommunityCommentRepository;
import org.sopt.makers.internal.community.repository.post.CommunityPostLikeRepository;
import org.sopt.makers.internal.community.repository.post.CommunityPostRepository;
import org.sopt.makers.internal.community.repository.post.DeletedCommunityPostRepository;
import org.sopt.makers.internal.community.service.SopticleScrapedService;
import org.sopt.makers.internal.community.service.anonymous.AnonymousProfileService;
import org.sopt.makers.internal.community.service.category.CategoryRetriever;
import org.sopt.makers.internal.community.service.category.CommunityCategoryPolicy;
import org.sopt.makers.internal.community.service.comment.CommunityCommentLikeService;
import org.sopt.makers.internal.community.service.comment.CommunityCommentService;
import org.sopt.makers.internal.community.service.member.CommunityMemberVoAssembler;
import org.sopt.makers.internal.exception.PlaygroundException;
import org.sopt.makers.internal.exception.BadRequestException;
import org.sopt.makers.internal.community.service.post.crew.CachedCrewPost;
import org.sopt.makers.internal.community.service.post.crew.CrewMeetingCandidate;
import org.sopt.makers.internal.community.service.post.crew.CrewMeetingFeedCache;
import org.sopt.makers.internal.community.service.post.crew.CrewMeetingFeedCacheService;
import org.sopt.makers.internal.community.service.post.crew.CrewMeetingFetchResult;
import org.sopt.makers.internal.external.makers.CrewPost;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.PlatformService;
import org.sopt.makers.internal.external.platform.SoptActivity;
import org.sopt.makers.internal.external.pushNotification.message.SimplePushNotificationMessage;
import org.sopt.makers.internal.common.event.PushNotificationEvent;
import org.sopt.makers.internal.external.slack.SlackClient;
import org.sopt.makers.internal.external.slack.SlackMessageUtil;
import org.sopt.makers.internal.internal.dto.InternalLatestPostResponse;
import org.sopt.makers.internal.internal.dto.InternalPopularPostResponse;
import org.sopt.makers.internal.member.constants.MakersMemberId;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.repository.MemberBlockRepository;
import org.sopt.makers.internal.member.service.MemberRetriever;
import org.sopt.makers.internal.member.service.career.MemberCareerRetriever;
import org.sopt.makers.internal.vote.dto.response.VoteResponse;
import org.sopt.makers.internal.vote.service.VoteService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.format.DateTimeFormatter;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommunityPostService {

    private final AnonymousProfileService anonymousProfileService;
    private final SopticleScrapedService sopticleScrapedService;
    private final VoteService voteService;
    private final ApplicationEventPublisher eventPublisher;
    private final PlatformService platformService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CrewMeetingFeedCacheService crewMeetingFeedCacheService;

    private final CommunityPostModifier communityPostModifier;

    private final MemberRetriever memberRetriever;
    private final CategoryRetriever categoryRetriever;
    private final CommunityPostRetriever communityPostRetriever;
    private final MemberCareerRetriever memberCareerRetriever;
    private final CommunityMemberVoAssembler communityMemberVoAssembler;

    private final CommunityCommentRepository communityCommentRepository;
    private final CommunityPostLikeRepository communityPostLikeRepository;
    private final CommunityPostRepository communityPostRepository;
    private final CommunityQueryRepository communityQueryRepository;
    private final DeletedCommunityPostRepository deletedCommunityPostRepository;
    private final DeletedCommunityCommentRepository deletedCommunityCommentRepository;
    private final ReportPostRepository reportPostRepository;
    private final MemberBlockRepository memberBlockRepository;
    private final AnonymousProfileRetriever anonymousProfileRetriever;

    private final CommunityCommentService communityCommentService;
    private final CommunityCommentLikeService commentLikeService;

    private final CommunityMapper communityMapper;
    private final CommunityResponseMapper communityResponseMapper;

    private final CommunityFeedCursorCodec communityFeedCursorCodec;
    private final CommunityCategoryPolicy communityCategoryPolicy;

    private final SlackMessageUtil slackMessageUtil;
    private final SlackClient slackClient;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    private final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int MIN_POINTS_FOR_HOT_POST = 10;

    private static final int HOME_PREVIEW_LIMIT = 3;
    private static final int POPULAR_LOOKBACK_DAYS = 30;
    private static final int POPULAR_COMMENT_WEIGHT = 5;
    private static final int POPULAR_LIKE_WEIGHT = 3;
    private static final int POPULAR_MEETING_MAX_PAGE = 2;

    private static final String VIEW_COUNT_PREFIX = "post:view:";
    private static final Duration VIEW_COUNT_TTL = Duration.ofHours(24);

    private record FeedCandidate(
        CommunityPostSourceType sourceType,
        LocalDateTime createdAt,
        Long communityPostId,
        Integer meetingConsumedCount,
        PostResponse response
    ) {
    }

    private record PopularPreviewCandidate(
        CommunityPostSourceType sourceType,
        int score,
        LocalDateTime createdAt,
        Long id,
        PopularPostResponse response
    ) {
    }

    private record RecentPreviewCandidate(
        CommunityPostSourceType sourceType,
        LocalDateTime createdAt,
        Long id,
        RecentPostResponse response
    ) {
    }

    // 커뮤니티글 목록 조회 entrypoint
    public PostAllResponse getPosts(
        Long userId,
        CommunityPostListCategory category,
        CommunityPostListFilter filter,
        Boolean isBlockedOn,
        Integer limit,
        String cursor
    ) {
        int normalizedLimit = normalizeLimit(limit);
        CommunityFeedCursor decodedCursor = communityFeedCursorCodec.decodeOrInitial(cursor);

        List<CommunityCategoryCode> categoryCodes = communityCategoryPolicy.resolveCategoryCodes(category, filter);

        if (category == CommunityPostListCategory.FREE) {
            return getFreePostsWithMeetingPosts(
                userId,
                isBlockedOn,
                normalizedLimit,
                decodedCursor,
                categoryCodes
            );
        }

        return getCommunityOnlyPosts(
            userId,
            category,
            isBlockedOn,
            normalizedLimit,
            decodedCursor,
            categoryCodes
        );
    }

    // Playground DB 글만 조회
    private PostAllResponse getCommunityOnlyPosts(
        Long userId,
        CommunityPostListCategory category,
        Boolean isBlockedOn,
        Integer limit,
        CommunityFeedCursor cursor,
        List<CommunityCategoryCode> categoryCodes
    ) {
        List<CategoryPostMemberDao> postDaos = communityQueryRepository.findPostsByCategoryCodes(
            categoryCodes,
            cursor.community(),
            cursor.snapshotTime(),
            limit + 1,
            userId,
            isBlockedOn
        );

        boolean hasNext = postDaos.size() > limit;

        List<CategoryPostMemberDao> slicedPostDaos = hasNext
            ? postDaos.subList(0, limit)
            : postDaos;

        List<CommunityPostMemberVo> postVos = toCommunityPostMemberVos(slicedPostDaos, userId);
        List<PostResponse> responses = toPostResponses(postVos, userId, isBlockedOn);

        CommunityDbCursor nextDbCursor = null;

        if (!slicedPostDaos.isEmpty()) {
            CommunityPost lastPost = slicedPostDaos.get(slicedPostDaos.size() - 1).post();
            nextDbCursor = new CommunityDbCursor(lastPost.getCreatedAt(), lastPost.getId());
        }

        String nextCursor = null;

        if (hasNext && nextDbCursor != null) {
            nextCursor = communityFeedCursorCodec.encode(
                new CommunityFeedCursor(
                    cursor.snapshotTime(),
                    nextDbCursor,
                    cursor.safeMeetingConsumedCount()
                )
            );
        }

        return new PostAllResponse(category, hasNext, nextCursor, responses);
    }

    // 자유 + 모임(Crew) merge 조회
    private PostAllResponse getFreePostsWithMeetingPosts(
        Long userId,
        Boolean isBlockedOn,
        Integer limit,
        CommunityFeedCursor cursor,
        List<CommunityCategoryCode> freeCategoryCodes
    ) {
        List<CategoryPostMemberDao> communityPostDaos = communityQueryRepository.findPostsByCategoryCodes(
            freeCategoryCodes,
            cursor.community(),
            cursor.snapshotTime(),
            limit + 1,
            userId,
            isBlockedOn
        );

        List<CommunityPostMemberVo> communityPostVos = toCommunityPostMemberVos(communityPostDaos, userId);
        List<PostResponse> communityResponses = toPostResponses(communityPostVos, userId, isBlockedOn);

        CrewMeetingFetchResult meetingFetchResult = fetchMeetingResponses(
            userId,
            cursor.snapshotTime(),
            cursor.safeMeetingConsumedCount(),
            limit + 1
        );

        List<FeedCandidate> candidates = new ArrayList<>();

        for (int index = 0; index < communityResponses.size(); index++) {
            CommunityPost post = communityPostDaos.get(index).post();

            candidates.add(new FeedCandidate(
                CommunityPostSourceType.COMMUNITY,
                post.getCreatedAt(),
                post.getId(),
                null,
                communityResponses.get(index)
            ));
        }

        for (CrewMeetingCandidate meetingCandidate : meetingFetchResult.candidates()) {
            candidates.add(new FeedCandidate(
                CommunityPostSourceType.MEETING,
                meetingCandidate.createdAt(),
                null,
                meetingCandidate.nextConsumedCount(),
                meetingCandidate.response()
            ));
        }

        List<FeedCandidate> sortedCandidates = candidates.stream()
            .sorted(
                Comparator.comparing(FeedCandidate::createdAt)
                    .reversed()
                    .thenComparing(candidate -> candidate.sourceType().name())
                    .thenComparing(candidate -> candidate.response().id(), Comparator.reverseOrder())
            )
            .toList();

        boolean hasNext = sortedCandidates.size() > limit || meetingFetchResult.hasMore();

        List<FeedCandidate> slicedCandidates = sortedCandidates.size() > limit
            ? sortedCandidates.subList(0, limit)
            : sortedCandidates;

        List<PostResponse> responses = slicedCandidates.stream()
            .map(FeedCandidate::response)
            .toList();

        CommunityDbCursor nextDbCursor = cursor.community();
        int nextMeetingConsumedCount = cursor.safeMeetingConsumedCount();

        Optional<FeedCandidate> lastCommunityCandidate = slicedCandidates.stream()
            .filter(candidate -> candidate.sourceType() == CommunityPostSourceType.COMMUNITY)
            .reduce((previous, current) -> current);

        if (lastCommunityCandidate.isPresent()) {
            FeedCandidate candidate = lastCommunityCandidate.get();
            nextDbCursor = new CommunityDbCursor(candidate.createdAt(), candidate.communityPostId());
        }

        Optional<FeedCandidate> lastMeetingCandidate = slicedCandidates.stream()
            .filter(candidate -> candidate.sourceType() == CommunityPostSourceType.MEETING)
            .reduce((previous, current) -> current);

        if (lastMeetingCandidate.isPresent()) {
            nextMeetingConsumedCount = lastMeetingCandidate.get().meetingConsumedCount();
        }

        String nextCursor = null;

        if (hasNext) {
            nextCursor = communityFeedCursorCodec.encode(
                new CommunityFeedCursor(
                    cursor.snapshotTime(),
                    nextDbCursor,
                    nextMeetingConsumedCount
                )
            );
        }

        return new PostAllResponse(
            CommunityPostListCategory.FREE,
            hasNext,
            nextCursor,
            responses
        );
    }

    // Crew 모임 글 fetch
    private CrewMeetingFetchResult fetchMeetingResponses(
        Long userId,
        LocalDateTime snapshotTime,
        int alreadyConsumedCount,
        int requiredCount
    ) {
        CrewMeetingFeedCache cache = crewMeetingFeedCacheService.getOrBuildCache(
            userId,
            snapshotTime,
            alreadyConsumedCount + requiredCount
        );

        List<CachedCrewPost> cachedPosts = cache.safePosts();

        int fromIndex = Math.min(alreadyConsumedCount, cachedPosts.size());
        int toIndex = Math.min(fromIndex + requiredCount, cachedPosts.size());

        List<CrewMeetingCandidate> candidates = new ArrayList<>();

        for (int index = fromIndex; index < toIndex; index++) {
            CachedCrewPost cachedCrewPost = cachedPosts.get(index);
            CrewPost crewPost = cachedCrewPost.toCrewPost();
            PostResponse response = communityResponseMapper.toPostResponse(crewPost, userId);

            candidates.add(new CrewMeetingCandidate(
                crewPost.createdDate(),
                index + 1,
                response
            ));
        }

        boolean hasMore = cache.hasMorePage() || cachedPosts.size() > toIndex;

        return new CrewMeetingFetchResult(candidates, hasMore);
    }

    @Transactional(readOnly = true)
    public PostDetailData getPostById(Long memberId, Long postId, Boolean isBlockedOn) {
        val postDao = communityQueryRepository.getPostById(postId);
        if (Objects.isNull(postDao)) throw new BadRequestException("존재하지 않는 postId입니다.");

        val authorId = postDao.member().getId();
        if (isBlockedOn && !Objects.equals(memberId, authorId)) {
            val blocker = memberRetriever.findMemberById(memberId);
            val blockedMember = memberRetriever.findMemberById(authorId);

            if (memberBlockRepository.existsByBlockerAndBlockedMember(blocker, blockedMember)) {
                memberRetriever.checkBlockedMember(blocker, blockedMember);
            }
        }

        val authorDetails = platformService.getInternalUser(authorId);
        val authorCareer = memberCareerRetriever.findMemberLastCareerByMemberId(authorId);
        val voteResponse = voteService.getVoteByPostId(postId, memberId);

        val category = postDao.category();
        if (category != null) {
            Hibernate.initialize(category.getParent());
        }

        return new PostDetailData(postDao.post(), authorDetails, authorCareer, category, voteResponse);
    }

    @Transactional
    public PostSaveResponse createPost(Long writerId, PostSaveRequest request) {
        Member member = memberRetriever.findMemberById(writerId);
        CommunityPost post = createCommunityPostBasedOnCategory(member, request);

        if(Objects.nonNull(request.vote())) {
            voteService.createVote(post, request.vote());
        }
        if(Objects.nonNull(request.mention())) {
            sendMentionPushNotification(post.getTitle(), request.isBlindWriter(), request.mention());
        }

        handleBlindWriter(request, member, post);
        sendSlackNotificationForNonMakers(member, post);

        return communityResponseMapper.toPostSaveResponse(post);
    }

    private void sendMentionPushNotification(String postTitle, boolean isBlindWriter, MentionRequest mentionRequest) {
        String title = "✏️게시글에서 회원님이 언급됐어요.";
        String writerName = isBlindWriter ? "익명" : mentionRequest.writerName();
        String content = "[" + writerName + "의 글] : \"" + postTitle + "\"";

        SimplePushNotificationMessage message = SimplePushNotificationMessage.of(
                title, content, mentionRequest.userIds(), mentionRequest.webLink()
        );
        eventPublisher.publishEvent(PushNotificationEvent.of(message));
    }

    @Transactional
    public PostUpdateResponse updatePost(Long writerId, PostUpdateRequest request) {
        Member member = memberRetriever.findMemberById(writerId);
        CommunityPost post = communityPostRetriever.findCommunityPostById(request.postId());
        Category category = categoryRetriever.findActiveCategoryByCode(request.categoryCode());

        validatePostOwner(member.getId(), post.getMember().getId());

        if (communityCategoryPolicy.isSopticleCategoryCode(category.getCode())) {
            SopticleScrapedResponse scrapedResponse = sopticleScrapedService.getSopticleMetaData(request.link());

            post.updatePost(
                category,
                scrapedResponse.title(),
                scrapedResponse.description(),
                List.of(scrapedResponse.thumbnailUrl()),
                false,
                scrapedResponse.url()
            );
        } else {
            post.updatePost(
                category,
                request.title(),
                request.content(),
                request.images(),
                request.isBlindWriter(),
                ""
            );
        }

        communityPostRepository.save(post);

        if (Objects.nonNull(request.mention())) {
            sendMentionPushNotification(post.getTitle(), post.getIsBlindWriter(), request.mention());
        }

        return communityResponseMapper.toPostUpdateResponse(post);
    }

    @Transactional
    public void deletePost(Long postId, Long memberId) {
        CommunityPost post = communityPostRetriever.findCommunityPostById(postId);
        validatePostOwner(memberId, post.getMember().getId());

        val deletedPost = communityMapper.toDeleteCommunityPost(post);
        deletedCommunityPostRepository.save(deletedPost);
        post.getComments().stream().map(communityMapper::toDeleteCommunityComment)
                .forEach(deletedCommunityCommentRepository::save);
        communityPostRepository.delete(post);
    }

    public void increaseHit(Long userId, List<Long> postIdList) {
        for (Long postId : postIdList) {
            String redisKey = generateRedisKey(userId, postId);
            Boolean hasViewed = redisTemplate.hasKey(redisKey);

            if (hasViewed) {
                continue;
            }

            redisTemplate.opsForValue().set(redisKey, "1", VIEW_COUNT_TTL);

            try {
                communityPostModifier.increaseHitTransactional(postId);
            } catch (Exception e) {
                log.error("조회수 증가 실패. Redis 키 삭제 수행 (보상 트랜잭션). postId: {}, userId: {}, redisKey: {}",
                    postId, userId, redisKey, e);
                redisTemplate.delete(redisKey);
            }
        }
    }

    @Transactional
    public void reportPost(Long memberId, Long postId) {
        CommunityPost post = communityPostRetriever.findCommunityPostById(postId);
        InternalUserDetails userDetails = platformService.getInternalUser(memberId);

        try {
            if (Objects.equals(activeProfile, "prod")) {
                val slackRequest = createReportSlackRequest(post.getId(), userDetails.name());
                slackClient.postReportMessage(slackRequest.toString());
            }
        } catch (RuntimeException ex) {
            log.error("슬랙 요청이 실패했습니다 : " + ex.getMessage());
        }

        reportPostRepository.save(ReportPost.builder()
                .reporterId(memberId)
                .postId(postId)
                .createdAt(LocalDateTime.now(KST))
                .build());
    }

    @Transactional
    public void likePost(Long memberId, Long postId) {
        Member member = memberRetriever.findMemberById(memberId);
        CommunityPost post = communityPostRetriever.findCommunityPostById(postId);

        communityPostRetriever.checkAlreadyLikedPost(memberId, postId);

        communityPostLikeRepository.save(CommunityPostLike.builder().member(member).post(post).build());
    }

    @Transactional
    public void unlikePost(Long memberId, Long postId) {
        CommunityPostLike communityPostLike = communityPostRetriever.findCommunityPostLike(memberId, postId);
        communityPostLikeRepository.delete(communityPostLike);
    }

    @Transactional(readOnly = true)
    public Boolean isLiked(Long memberId, Long postId) {
        memberRetriever.checkExistsMemberById(memberId);
        communityPostRetriever.checkExistsCommunityPostById(postId);

        return communityPostLikeRepository.existsByMemberIdAndPostId(memberId, postId);
    }

    @Transactional(readOnly = true)
    public Integer getLikes(Long postId) {
        communityPostRetriever.checkExistsCommunityPostById(postId);

        return communityPostLikeRepository.countAllByPostId(postId);
    }

    @Transactional(readOnly = true)
    public AnonymousProfile getAnonymousPostProfile(Long postId) {
        return anonymousProfileRetriever.findByPostId(postId).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<CommunityPost> getTodayPosts() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime startOfDay = yesterday.atStartOfDay().minusHours(9);
        LocalDateTime endOfDay = yesterday.atTime(LocalTime.MAX).minusHours(9);
        return communityPostRepository.findAllByCreatedAtBetween(startOfDay, endOfDay);
    }

    @Transactional(readOnly = true)
    public CommunityPost getRecentHotPost() {
        return communityQueryRepository.findRecentHotPost();
    }

    @Transactional(readOnly = true)
    public PostCategoryDao getRecentPostByCategoryGroup(CommunityCategoryGroup categoryGroup) {
        return communityPostRepository.findRecentPostByCategoryGroup(categoryGroup);
    }

    @Transactional(readOnly = true)
    public List<PopularPostResponse> getPopularPosts(Long userId, int limitCount) {
        int normalizedLimit = normalizePreviewLimit(limitCount);

        LocalDateTime snapshotTime = LocalDateTime.now()
            .withSecond(0)
            .withNano(0);

        LocalDateTime since = snapshotTime.minusDays(POPULAR_LOOKBACK_DAYS);

        List<CommunityPost> communityPosts = communityQueryRepository.findPopularCandidatePosts(since);

        Map<Long, AnonymousProfile> anonymousProfileMap = getAnonymousProfileMap(communityPosts);

        List<Long> postIds = communityPosts.stream()
            .map(CommunityPost::getId)
            .toList();

        Map<Long, Integer> postLikeCountMap = getPostLikeCountMap(postIds);
        Map<Long, Integer> postCommentCountMap = getPostCommentCountMap(postIds);

        List<Long> authorIds = communityPosts.stream()
            .map(post -> post.getMember().getId())
            .distinct()
            .toList();

        Map<Long, InternalUserDetails> userDetailsMap = platformService.getInternalUserDetailsMap(authorIds);

        List<PopularPreviewCandidate> candidates = new ArrayList<>();

        for (CommunityPost post : communityPosts) {
            Long postId = post.getId();
            Long authorId = post.getMember().getId();

            AnonymousProfile anonymousProfile = anonymousProfileMap.get(postId);
            InternalUserDetails userDetails = userDetailsMap.get(authorId);

            if (
                userDetails == null
                    && !(Boolean.TRUE.equals(post.getIsBlindWriter()) && anonymousProfile != null)
            ) {
                continue;
            }

            int likeCount = postLikeCountMap.getOrDefault(postId, 0);
            int commentCount = postCommentCountMap.getOrDefault(postId, 0);
            int score = calculatePopularScore(post.getHits(), commentCount, likeCount);

            PopularPostResponse response = communityResponseMapper.toPopularPostResponse(
                post,
                anonymousProfile,
                userDetails,
                likeCount,
                commentCount,
                communityCategoryPolicy.resolvePreviewTag(post.getCategory())
            );

            candidates.add(new PopularPreviewCandidate(
                CommunityPostSourceType.COMMUNITY,
                score,
                post.getCreatedAt(),
                post.getId(),
                response
            ));
        }

        List<CrewPost> meetingPosts = getMeetingPostsForPopular(userId, since, snapshotTime);

        for (CrewPost crewPost : meetingPosts) {
            int score = calculatePopularScore(
                crewPost.viewCount(),
                crewPost.commentCount(),
                crewPost.likeCount()
            );

            candidates.add(new PopularPreviewCandidate(
                CommunityPostSourceType.MEETING,
                score,
                crewPost.createdDate(),
                crewPost.id(),
                communityResponseMapper.toPopularPostResponse(crewPost)
            ));
        }

        return candidates.stream()
            .sorted(
                Comparator.comparingInt(PopularPreviewCandidate::score)
                    .reversed()
                    .thenComparing(PopularPreviewCandidate::createdAt, Comparator.reverseOrder())
                    .thenComparing(candidate -> candidate.sourceType().name())
                    .thenComparing(PopularPreviewCandidate::id, Comparator.reverseOrder())
            )
            .limit(normalizedLimit)
            .map(PopularPreviewCandidate::response)
            .toList();
    }

    private int calculatePopularScore(int viewCount, int commentCount, int likeCount) {
        return viewCount
            + commentCount * POPULAR_COMMENT_WEIGHT
            + likeCount * POPULAR_LIKE_WEIGHT;
    }

    @Transactional(readOnly = true)
    public List<InternalPopularPostResponse> getPopularPostsForInternal(int limitCount) {
        List<CommunityPost> posts = getPopularPostsBase(limitCount);
        Map<Long, AnonymousProfile> anonymousProfileMap = getAnonymousProfileMap(posts);

        List<Long> authorIds = posts.stream()
            .map(post -> post.getMember().getId())
            .distinct()
            .toList();

        Map<Long, InternalUserDetails> userDetailsMap = platformService.getInternalUserDetailsMap(authorIds);

        String baseUrl = activeProfile.equals("prod")
            ? "https://playground.sopt.org/?feed="
            : "https://sopt-internal-dev.pages.dev/?feed=";

        return IntStream.range(0, posts.size())
            .mapToObj(index -> {
                CommunityPost post = posts.get(index);
                Long authorId = post.getMember().getId();

                return communityResponseMapper.toInternalPopularPostResponse(
                    post,
                    anonymousProfileMap.get(post.getId()),
                    userDetailsMap.get(authorId),
                    resolveRootCategoryName(post.getCategory()),
                    index + 1,
                    baseUrl
                );
            })
            .toList();
    }

    private List<CommunityPost> getPopularPostsBase(int limitCount) {
        List<CommunityPost> posts = communityQueryRepository.findPopularPosts(limitCount);

        if (posts.isEmpty()) {
            throw new PlaygroundException("최근 한 달 내에 작성된 게시물이 없습니다.");
        }
        return posts;
    }

    @Transactional(readOnly = true)
    public CommunityPost findTodayHotPost(List<CommunityPost> posts) {
        return posts.stream()
                .map(this::createPostWithPoints)
                .filter(post -> post.points() >= MIN_POINTS_FOR_HOT_POST)
                .max(Comparator.comparingInt(PostWithPoints::points)
                        .thenComparingInt(PostWithPoints::hits))
                .map(PostWithPoints::post)
                .orElse(null);
    }

    @Transactional
    public void saveHotPost(CommunityPost post) {
        communityQueryRepository.updateIsHotByPostId(post.getId());
    }

    @Transactional(readOnly = true)
    public List<SopticlePostResponse> getRecentSopticlePosts() {
        List<CommunityCategoryCode> sopticleCodes = communityCategoryPolicy.resolveCategoryCodes(
            CommunityPostListCategory.SOPTICLE,
            CommunityPostListFilter.ALL
        );

        List<CommunityPost> posts = communityPostRepository
            .findTop5ByCategory_CodeInOrderByCreatedAtDesc(sopticleCodes);

        List<Long> authorIds = posts.stream()
            .map(post -> post.getMember().getId())
            .distinct()
            .toList();

        Map<Long, MemberVo> memberVoMap = communityMemberVoAssembler.getMemberVoMap(authorIds);

        return posts.stream()
            .map(post -> {
                Long authorId = post.getMember().getId();
                return communityResponseMapper.toSopticlePostResponse(post, memberVoMap.get(authorId));
            })
            .toList();
    }

    @Transactional(readOnly = true)
    public List<RecentPostResponse> getRecentPosts(Long memberId) {
        List<CommunityPost> communityPosts = communityPostRepository
            .findTop3ByCategory_CategoryGroupInOrderByCreatedAtDesc(
                List.of(CommunityCategoryGroup.FREE, CommunityCategoryGroup.PROMOTION)
            );

        List<Long> postIds = communityPosts.stream()
            .map(CommunityPost::getId)
            .toList();

        Map<Long, Integer> postLikeCountMap = getPostLikeCountMap(postIds);
        Map<Long, Integer> postCommentCountMap = getPostCommentCountMap(postIds);
        Map<Long, VoteResponse> voteResponseMap = voteService.getVoteMapByPostIds(postIds, memberId);

        List<RecentPreviewCandidate> candidates = new ArrayList<>();

        for (CommunityPost post : communityPosts) {
            Long postId = post.getId();

            VoteResponse vote = voteResponseMap.get(postId);
            Integer totalVoteCount = Objects.nonNull(vote) ? vote.totalParticipants() : null;

            RecentPostResponse response = communityResponseMapper.toRecentPostResponse(
                post,
                postLikeCountMap.getOrDefault(postId, 0),
                postCommentCountMap.getOrDefault(postId, 0),
                communityCategoryPolicy.resolvePreviewTag(post.getCategory()),
                totalVoteCount
            );

            candidates.add(new RecentPreviewCandidate(
                CommunityPostSourceType.COMMUNITY,
                post.getCreatedAt(),
                post.getId(),
                response
            ));
        }

        List<CrewPost> meetingPosts = getMeetingPostsForPreview(memberId, HOME_PREVIEW_LIMIT);

        for (CrewPost crewPost : meetingPosts) {
            candidates.add(new RecentPreviewCandidate(
                CommunityPostSourceType.MEETING,
                crewPost.createdDate(),
                crewPost.id(),
                communityResponseMapper.toRecentPostResponse(crewPost)
            ));
        }

        return candidates.stream()
            .sorted(
                Comparator.comparing(RecentPreviewCandidate::createdAt)
                    .reversed()
                    .thenComparing(candidate -> candidate.sourceType().name())
                    .thenComparing(RecentPreviewCandidate::id, Comparator.reverseOrder())
            )
            .limit(HOME_PREVIEW_LIMIT)
            .map(RecentPreviewCandidate::response)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<InternalLatestPostResponse> getInternalLatestPosts() {
        List<CommunityPostListCategory> topLevelCategories = List.of(
            CommunityPostListCategory.FREE,
            CommunityPostListCategory.PROMOTION,
            CommunityPostListCategory.SOPTICLE
        );

        List<CommunityPost> latestPosts = new ArrayList<>();

        for (CommunityPostListCategory listCategory : topLevelCategories) {
            List<CommunityCategoryCode> categoryCodes = communityCategoryPolicy.resolveCategoryCodes(
                listCategory,
                listCategory == CommunityPostListCategory.FREE ? null : CommunityPostListFilter.ALL
            );

            communityPostRepository.findFirstByCategory_CodeInOrderByCreatedAtDesc(categoryCodes)
                .ifPresent(latestPosts::add);
        }

        List<Long> authorIds = latestPosts.stream()
            .map(post -> post.getMember().getId())
            .distinct()
            .toList();

        Map<Long, InternalUserDetails> userDetailsMap =
            platformService.getInternalUserDetailsMap(authorIds);

        Map<Long, AnonymousProfile> anonymousProfileMap =
            getAnonymousProfileMap(latestPosts);

        List<InternalLatestPostResponse> responses = new ArrayList<>();

        String baseUrl = activeProfile.equals("prod")
            ? "https://playground.sopt.org/?feed="
            : "https://sopt-internal-dev.pages.dev/?feed=";

        for (CommunityPost post : latestPosts) {
            Long authorId = post.getMember().getId();
            InternalUserDetails userDetails = userDetailsMap.get(authorId);

            if (userDetails == null) {
                continue;
            }

            SoptActivity latestActivity = userDetails.soptActivities() == null
                ? null
                : userDetails.soptActivities().stream()
                  .max(Comparator.comparing(SoptActivity::generation)
                       .thenComparing(activity -> !activity.isSopt()))
                  .orElse(null);

            String categoryName = resolveRootCategoryName(post.getCategory());

            Optional<AnonymousProfile> anonymousProfile = Boolean.TRUE.equals(post.getIsBlindWriter())
                ? Optional.ofNullable(anonymousProfileMap.get(post.getId()))
                : Optional.empty();

            responses.add(InternalLatestPostResponse.of(
                post,
                latestActivity,
                userDetails,
                categoryName,
                anonymousProfile,
                baseUrl
            ));
        }

        return responses;
    }

    private List<CommunityPostMemberVo> toCommunityPostMemberVos(
        List<CategoryPostMemberDao> postDaos,
        Long memberId
    ) {
        if (postDaos == null || postDaos.isEmpty()) {
            return List.of();
        }

        List<Long> authorIds = postDaos.stream()
            .map(postDao -> postDao.member().getId())
            .distinct()
            .toList();

        List<Long> postIds = postDaos.stream()
            .map(postDao -> postDao.post().getId())
            .distinct()
            .toList();

        Map<Long, MemberVo> memberVoMap = communityMemberVoAssembler.getMemberVoMap(authorIds);
        Map<Long, VoteResponse> voteResponseMap = voteService.getVoteMapByPostIds(postIds, memberId);

        return postDaos.stream()
            .map(postDao -> {
                Long authorId = postDao.member().getId();
                Long postId = postDao.post().getId();

                val memberVo = memberVoMap.get(authorId);
                val categoryVo = communityResponseMapper.toCategoryResponse(postDao.category());
                val postVo = communityResponseMapper.toPostVo(
                    postDao.post(),
                    postDao.category(),
                    voteResponseMap.get(postId)
                );

                return new CommunityPostMemberVo(memberVo, postVo, categoryVo);
            })
            .toList();
    }

    private List<PostResponse> toPostResponses(
        List<CommunityPostMemberVo> posts,
        Long userId,
        Boolean isBlockedOn
    ) {
        if (posts == null || posts.isEmpty()) {
            return List.of();
        }

        List<Long> postIds = posts.stream()
            .map(post -> post.post().id())
            .toList();

        Map<Long, List<CommentInfo>> postCommentsMap = communityCommentService.getPostCommentMap(
            postIds,
            userId,
            isBlockedOn
        );

        List<Long> allCommentIds = postCommentsMap.values().stream()
            .flatMap(List::stream)
            .map(commentInfo -> commentInfo.commentDao().comment().getId())
            .toList();

        Map<Long, Boolean> commentLikedMap = commentLikeService.getLikedMapByCommentIds(userId, allCommentIds);
        Map<Long, Integer> commentLikeCountMap = commentLikeService.getLikeCountMapByCommentIds(allCommentIds);

        Map<Long, AnonymousProfile> anonymousPostProfileMap = communityQueryRepository.getAnonymousProfilesByPostId(postIds);
        Map<Long, Boolean> postLikedMap = getPostLikedMap(userId, postIds);
        Map<Long, Integer> postLikeCountMap = getPostLikeCountMap(postIds);

        return posts.stream()
            .map(post -> {
                Long postId = post.post().id();

                List<CommentInfo> comments = postCommentsMap.getOrDefault(postId, List.of());
                AnonymousProfile anonymousPostProfile = anonymousPostProfileMap.get(postId);
                Boolean isLiked = postLikedMap.getOrDefault(postId, false);
                Integer likes = postLikeCountMap.getOrDefault(postId, 0);

                return communityResponseMapper.toPostResponse(
                    post,
                    comments,
                    userId,
                    anonymousPostProfile,
                    isLiked,
                    likes,
                    commentLikedMap,
                    commentLikeCountMap
                );
            })
            .toList();
    }

    private Map<Long, Boolean> getPostLikedMap(Long userId, List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Map.of();
        }

        List<Long> likedPostIds = communityPostLikeRepository.findLikedPostIdsByMemberIdAndPostIds(
            userId,
            postIds
        );

        Set<Long> likedPostIdSet = Set.copyOf(likedPostIds);

        return postIds.stream()
            .collect(Collectors.toMap(
                postId -> postId,
                likedPostIdSet::contains
            ));
    }

    private Map<Long, Integer> getPostLikeCountMap(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Map.of();
        }

        return communityPostLikeRepository.countLikesByPostIds(postIds).stream()
            .collect(Collectors.toMap(
                CommunityPostLikeRepository.PostLikeCountProjection::getPostId,
                projection -> projection.getLikeCount().intValue()
            ));
    }

    private Map<Long, Integer> getPostCommentCountMap(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Map.of();
        }

        return communityCommentRepository.countCommentsByPostIds(postIds).stream()
            .collect(Collectors.toMap(
                CommunityCommentRepository.PostCommentCountProjection::getPostId,
                projection -> projection.getCommentCount().intValue()
            ));
    }

    private String resolveRootCategoryName(Category category) {
        if (category == null) {
            return "";
        }

        return category.getParent() == null
            ? category.getName()
            : category.getParent().getName();
    }

    private PostWithPoints createPostWithPoints(CommunityPost post) {
        int commentCount = communityCommentRepository.countAllByPostIdAndIsDeleted(post.getId(), false);
        int likeCount = communityPostLikeRepository.countAllByPostId(post.getId());
        int points = calculatePoints(commentCount, likeCount);
        return new PostWithPoints(post, points, post.getHits());
    }

    private int calculatePoints(int commentCount, int likeCount) {
        return commentCount * 2 + likeCount;
    }

    private JsonNode createReportSlackRequest(Long id, String name) {
        val rootNode = slackMessageUtil.getObjectNode();
        rootNode.put("text", "🚨글 신고 발생!🚨");

        val blocks = slackMessageUtil.getArrayNode();
        val textField = slackMessageUtil.createTextField("글 신고가 들어왔어요!");
        val contentNode = slackMessageUtil.createSection();

        val fields = slackMessageUtil.getArrayNode();
        fields.add(slackMessageUtil.createTextFieldNode("*신고자:*\n" + name));
        fields.add(slackMessageUtil.createTextFieldNode("*글 링크:*\n<https://playground.sopt.org/feed/" + id + "|글>"));
        contentNode.set("fields", fields);

        blocks.add(textField);
        blocks.add(contentNode);
        rootNode.set("blocks", blocks);
        return rootNode;
    }

    private JsonNode createPostSlackRequest(Long postId) {
        val rootNode = slackMessageUtil.getObjectNode();
        rootNode.put("text", "💙 비 메이커스 유저 글 작성");

        val blocks = slackMessageUtil.getArrayNode();
        val textField = slackMessageUtil.createTextField("비 메이커스 유저가 글을 작성했어요!");
        val contentNode = slackMessageUtil.createSection();

        val fields = slackMessageUtil.getArrayNode();
        fields.add(slackMessageUtil.createTextFieldNode("*글 링크:*\n<https://playground.sopt.org/feed/" + postId + "|링크>"));
        contentNode.set("fields", fields);

        blocks.add(textField);
        blocks.add(contentNode);
        rootNode.set("blocks", blocks);
        return rootNode;
    }

    private record PostWithPoints(CommunityPost post, int points, int hits) {
    }

    private void handleBlindWriter(PostSaveRequest request, Member member, CommunityPost post) {
        if (request.isBlindWriter()) {
            AnonymousProfile profile = anonymousProfileService.getOrCreateAnonymousProfile(member, post);
            post.registerAnonymousProfile(profile);
        }
    }

    private void sendSlackNotificationForNonMakers(Member member, CommunityPost post) {
        if (Objects.equals(activeProfile, "prod") && !MakersMemberId.getMakersMember().contains(member.getId())) {
            val slackRequest = createPostSlackRequest(post.getId());
            slackClient.postNotMakersMessage(slackRequest.toString());
        }
    }

    private CommunityPost createCommunityPostBasedOnCategory(Member member, PostSaveRequest request) {
        Category category = categoryRetriever.findActiveCategoryByCode(request.categoryCode());

        if (communityCategoryPolicy.isSopticleCategoryCode(category.getCode())) {
            SopticleScrapedResponse scrapedResponse = sopticleScrapedService.getSopticleMetaData(request.link());

            PostSaveRequest enrichedRequest = PostSaveRequest.builder()
                .categoryCode(request.categoryCode())
                .content(scrapedResponse.description())
                .images(List.of(scrapedResponse.thumbnailUrl()))
                .link(scrapedResponse.url())
                .title(scrapedResponse.title())
                .isBlindWriter(false)
                .build();

            return communityPostModifier.createCommunityPost(member, category, enrichedRequest);
        }

        return communityPostModifier.createCommunityPost(member, category, request);
    }

    private void validatePostOwner(Long memberId, Long postWriterId) {
        if (!Objects.equals(memberId, postWriterId)) {
            throw new BadRequestException("수정/삭제 권한이 없는 유저입니다.");
        }
    }

    private Map<Long, AnonymousProfile> getAnonymousProfileMap(List<CommunityPost> posts) {
        List<Long> postIds = posts.stream()
                .map(CommunityPost::getId)
                .distinct()
                .toList();
        return communityQueryRepository.getAnonymousProfilesByPostId(postIds);
    }

    private List<CrewPost> getMeetingPostsForPreview(Long userId, int requiredCount) {
        if (userId == null || requiredCount <= 0) {
            return List.of();
        }

        LocalDateTime snapshotTime = LocalDateTime.now()
            .withSecond(0)
            .withNano(0);

        CrewMeetingFeedCache cache = crewMeetingFeedCacheService.getOrBuildPreviewCache(
            userId,
            snapshotTime,
            requiredCount
        );

        return cache.safePosts().stream()
            .limit(requiredCount)
            .map(CachedCrewPost::toCrewPost)
            .toList();
    }

    private List<CrewPost> getMeetingPostsForPopular(
        Long userId,
        LocalDateTime since,
        LocalDateTime snapshotTime
    ) {
        if (userId == null) {
            return List.of();
        }

        CrewMeetingFeedCache cache = crewMeetingFeedCacheService.getOrBuildPopularPreviewCache(
            userId,
            snapshotTime,
            since,
            POPULAR_MEETING_MAX_PAGE
        );

        return cache.safePosts().stream()
            .map(CachedCrewPost::toCrewPost)
            .filter(post -> !post.createdDate().isAfter(snapshotTime))
            .filter(post -> !post.createdDate().isBefore(since))
            .toList();
    }

    private String generateRedisKey(Long userId, Long postId) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        return VIEW_COUNT_PREFIX + today + ":" + userId + ":" + postId;
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0 || limit > 50) {
            return 50;
        }

        return limit;
    }

    private int normalizePreviewLimit(int limit) {
        if (limit <= 0 || limit > HOME_PREVIEW_LIMIT) {
            return HOME_PREVIEW_LIMIT;
        }
        return limit;
    }
}
