package org.sopt.makers.internal.community.service.post;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.sopt.makers.internal.community.domain.CommunityPost;
import org.sopt.makers.internal.community.domain.CommunityPostLike;
import org.sopt.makers.internal.community.domain.ReportPost;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousPostProfile;
import org.sopt.makers.internal.community.domain.category.Category;
import org.sopt.makers.internal.community.dto.CategoryPostMemberDao;
import org.sopt.makers.internal.community.dto.CommunityPostMemberVo;
import org.sopt.makers.internal.community.dto.MemberVo;
import org.sopt.makers.internal.community.dto.PostCategoryDao;
import org.sopt.makers.internal.community.dto.PostDetailData;
import org.sopt.makers.internal.community.dto.request.MentionRequest;
import org.sopt.makers.internal.community.dto.request.PostSaveRequest;
import org.sopt.makers.internal.community.dto.request.PostUpdateRequest;
import org.sopt.makers.internal.community.dto.response.PopularPostResponse;
import org.sopt.makers.internal.community.dto.response.PostSaveResponse;
import org.sopt.makers.internal.community.dto.response.PostUpdateResponse;
import org.sopt.makers.internal.community.dto.response.RecentPostResponse;
import org.sopt.makers.internal.community.dto.response.SopticlePostResponse;
import org.sopt.makers.internal.community.dto.response.SopticleScrapedResponse;
import org.sopt.makers.internal.community.mapper.CommunityMapper;
import org.sopt.makers.internal.community.mapper.CommunityResponseMapper;
import org.sopt.makers.internal.community.repository.CommunityQueryRepository;
import org.sopt.makers.internal.community.repository.ReportPostRepository;
import org.sopt.makers.internal.community.repository.anonymous.AnonymousPostProfileRepository;
import org.sopt.makers.internal.community.repository.category.CategoryRepository;
import org.sopt.makers.internal.community.repository.comment.CommunityCommentRepository;
import org.sopt.makers.internal.community.repository.comment.DeletedCommunityCommentRepository;
import org.sopt.makers.internal.community.repository.post.CommunityPostLikeRepository;
import org.sopt.makers.internal.community.repository.post.CommunityPostRepository;
import org.sopt.makers.internal.community.repository.post.DeletedCommunityPostRepository;
import org.sopt.makers.internal.community.service.SopticleScrapedService;
import org.sopt.makers.internal.community.service.anonymous.AnonymousPostProfileService;
import org.sopt.makers.internal.community.service.category.CategoryRetriever;
import org.sopt.makers.internal.exception.BusinessLogicException;
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.PlatformService;
import org.sopt.makers.internal.external.platform.SoptActivity;
import org.sopt.makers.internal.external.pushNotification.PushNotificationService;
import org.sopt.makers.internal.external.slack.SlackClient;
import org.sopt.makers.internal.external.slack.SlackMessageUtil;
import org.sopt.makers.internal.internal.dto.InternalLatestPostResponse;
import org.sopt.makers.internal.internal.dto.InternalPopularPostResponse;
import org.sopt.makers.internal.member.domain.MakersMemberId;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.repository.MemberBlockRepository;
import org.sopt.makers.internal.member.service.MemberRetriever;
import org.sopt.makers.internal.member.service.career.MemberCareerRetriever;
import org.sopt.makers.internal.vote.dto.response.VoteResponse;
import org.sopt.makers.internal.vote.service.VoteService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.stream.Collectors.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommunityPostService {

    private final AnonymousPostProfileService anonymousPostProfileService;
    private final SopticleScrapedService sopticleScrapedService;
    private final VoteService voteService;
    private final PushNotificationService pushNotificationService;
    private final PlatformService platformService;

    private final CommunityPostModifier communityPostModifier;

    private final MemberRetriever memberRetriever;
    private final CategoryRetriever categoryRetriever;
    private final CommunityPostRetriever communityPostRetriever;
    private final MemberCareerRetriever memberCareerRetriever;

    private final CommunityCommentRepository communityCommentRepository;
    private final CommunityPostLikeRepository communityPostLikeRepository;
    private final CommunityPostRepository communityPostRepository;
    private final CommunityQueryRepository communityQueryRepository;
    private final DeletedCommunityPostRepository deletedCommunityPostRepository;
    private final DeletedCommunityCommentRepository deletedCommunityCommentRepository;
    private final ReportPostRepository reportPostRepository;
    private final MemberBlockRepository memberBlockRepository;
    private final AnonymousPostProfileRepository anonymousPostProfileRepository;
    private final CategoryRepository categoryRepository;

    private final CommunityMapper communityMapper;
    private final CommunityResponseMapper communityResponseMapper;

    private final SlackMessageUtil slackMessageUtil;
    private final SlackClient slackClient;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    private final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int MIN_POINTS_FOR_HOT_POST = 10;
    private static final long SOPTICLE_CATEGORY_ID = 21;

    @Transactional(readOnly = true)
    public List<CommunityPostMemberVo> getAllPosts(Long categoryId, Boolean isBlockedOn, Long memberId, Integer limit, Long cursor) {
        if (limit == null || limit >= 50) limit = 50;
        categoryRetriever.checkExistsCategoryById(categoryId);

        List<CategoryPostMemberDao> posts = communityQueryRepository.findAllParentCategoryPostByCursor(categoryId, limit, cursor, memberId, isBlockedOn);

        return posts.stream()
                .map(postDao -> {
                    val authorDetails = platformService.getInternalUser(postDao.member().getId());
                    val authorCareer = memberCareerRetriever.findMemberLastCareerByMemberId(postDao.member().getId());
                    val voteResponse = voteService.getVoteByPostId(postDao.post().getId(), memberId);
                    val memberVo = MemberVo.of(authorDetails, authorCareer);
                    val categoryVo = communityResponseMapper.toCategoryResponse(postDao.category());
                    val postVo = communityResponseMapper.toPostVo(postDao.post(), voteResponse);
                    return new CommunityPostMemberVo(memberVo, postVo, categoryVo);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public PostDetailData getPostById(Long memberId, Long postId, Boolean isBlockedOn) {
        val postDao = communityQueryRepository.getPostById(postId);
        if (Objects.isNull(postDao)) throw new ClientBadRequestException("존재하지 않는 postId입니다.");

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

        return new PostDetailData(postDao.post(), authorDetails, authorCareer, postDao.category(), voteResponse);
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

        pushNotificationService.sendPushNotification(title, content, mentionRequest.userIds(), mentionRequest.webLink());
    }

    @Transactional
    public PostUpdateResponse updatePost(Long writerId, PostUpdateRequest request) {
        Member member = memberRetriever.findMemberById(writerId);
        CommunityPost post = communityPostRetriever.findCommunityPostById(request.postId());
        categoryRetriever.checkExistsCategoryById(request.categoryId());
        validatePostOwner(member.getId(), post.getMember().getId());

        if (isSopticleCategory(request.categoryId())) {
            SopticleScrapedResponse scrapedResponse = sopticleScrapedService.getSopticleMetaData(request.link());
            post.updatePost(request.categoryId(), scrapedResponse.title(), scrapedResponse.description(), new String[] { scrapedResponse.thumbnailUrl() },
                            request.isQuestion(), request.isBlindWriter(), scrapedResponse.url());
        } else {
            post.updatePost(request.categoryId(), request.title(), request.content(), request.images(),
                            request.isQuestion(), request.isBlindWriter(), "");
        }

        communityPostRepository.save(post);

        if(Objects.nonNull(request.mention())) {
            sendMentionPushNotification(post.getTitle(), request.isBlindWriter(), request.mention());
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

    public void increaseHit(List<Long> postIdList) {
        for (Long postId : postIdList) {
            communityPostModifier.increaseHitTransactional(postId);
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
    public AnonymousPostProfile getAnonymousPostProfile(Long postId) {
        return anonymousPostProfileRepository.findAnonymousPostProfileByCommunityPostId(postId).orElse(null);
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
    public PostCategoryDao getRecentPostByCategory(String category) {
        return communityPostRepository.findRecentPostByCategory(category);
    }

    @Transactional(readOnly = true)
    public List<PopularPostResponse> getPopularPosts(int limitCount) {
        List<CommunityPost> posts = getPopularPostsBase(limitCount);

        Map<Long, String> categoryNameMap = categoryRetriever.getAllCategories().stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        return posts.stream()
                .map(post -> {
                    val authorDetails = platformService.getInternalUser(post.getMember().getId());
                    val anonymousProfile = post.getIsBlindWriter()
                            ? anonymousPostProfileRepository.findAnonymousPostProfileByCommunityPostId(post.getId()).orElse(null)
                            : null;
                    String categoryName = categoryNameMap.getOrDefault(post.getCategoryId(), "");
                    return communityResponseMapper.toPopularPostResponse(post, anonymousProfile, authorDetails, categoryName);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InternalPopularPostResponse> getPopularPostsForInternal(int limitCount) {
        List<CommunityPost> posts = getPopularPostsBase(limitCount);

        Map<Long, String> categoryNameMap = getCategoryNameMap(posts);
        Map<Long, AnonymousPostProfile> anonymousProfileMap = getAnonymousProfileMap(posts);

        return IntStream.range(0, posts.size())
                .mapToObj(idx -> {
                    CommunityPost post = posts.get(idx);
                    InternalUserDetails userDetails = platformService.getInternalUser(post.getMember().getId());
                    return communityResponseMapper.toInternalPopularPostResponse(
                            post,
                            anonymousProfileMap.get(post.getId()),
                            userDetails,
                            categoryNameMap.get(post.getCategoryId()),
                            idx + 1
                    );
                })
                .toList();
    }

    private List<CommunityPost> getPopularPostsBase(int limitCount) {
        List<CommunityPost> posts = communityQueryRepository.findPopularPosts(limitCount);

        if (posts.isEmpty()) {
            throw new BusinessLogicException("최근 한 달 내에 작성된 게시물이 없습니다.");
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
        List<CommunityPost> posts = communityPostRepository.findTop5ByCategoryIdOrderByCreatedAtDesc(SOPTICLE_CATEGORY_ID);
        return posts.stream()
                .map(post -> {
                    val authorDetails = platformService.getInternalUser(post.getMember().getId());
                    val authorCareer = memberCareerRetriever.findMemberLastCareerByMemberId(post.getMember().getId());
                    val memberVo = MemberVo.of(authorDetails, authorCareer);
                    return communityResponseMapper.toSopticlePostResponse(post, memberVo);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RecentPostResponse> getRecentPosts(Long memberId) {
        List<CommunityPost> posts = communityPostRepository.findTop5ByCategoryIdNotOrderByCreatedAtDesc(SOPTICLE_CATEGORY_ID);

        Map<Long, Category> categoryMap = getCategoryMap(posts); //n+1 문제 방지를 위해 미리 카테고리 조회

        return posts.stream()
                .map(post -> {
                    int likeCount = communityPostLikeRepository.countAllByPostId(post.getId());
                    int commentCount = communityCommentRepository.countAllByPostId(post.getId());
                    VoteResponse vote = voteService.getVoteByPostId(post.getId(), memberId);
                    Integer totalVoteCount = Objects.nonNull(vote) ? vote.totalParticipants() : null;
                    Category category = categoryMap.get(post.getCategoryId());

                    Long categoryId = null;
                    String categoryName = "";

                    if (Objects.nonNull(category)) {
                        categoryName = (category.getParent() != null) ? category.getParent().getName() : category.getName();
                        categoryId = (category.getParent() != null) ? category.getParent().getId() : category.getId();
                    }

                    return communityResponseMapper.toRecentPostResponse(
                            post, likeCount, commentCount, categoryId, categoryName, totalVoteCount
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InternalLatestPostResponse> getInternalLatestPosts() {
        final List<Long> topLevelCategoryIds = List.of(1L, 22L, 4L, 2L, 21L);

        Map<Long, String> categoryNameMap = categoryRetriever.findAllByIds(topLevelCategoryIds).stream()
                .collect(toMap(Category::getId, Category::getName));

        List<InternalLatestPostResponse> responses = new ArrayList<>();

        for (Long categoryId : topLevelCategoryIds) {
            List<Long> allCategoryIds = categoryRetriever.findAllDescendantIds(categoryId);

            Optional<CommunityPost> postOptional = communityPostRepository.findFirstByCategoryIdInOrderByCreatedAtDesc(allCategoryIds);

            postOptional.ifPresent(post -> {
                InternalUserDetails userDetails = platformService.getInternalUser(post.getMember().getId());
                SoptActivity latestActivity = userDetails.soptActivities().stream()
                        .max(Comparator.comparing(SoptActivity::generation))
                        .orElse(null);

                String categoryName = categoryNameMap.getOrDefault(categoryId, "");

                Optional<AnonymousPostProfile> anonymousProfile = Optional.empty();
                if (post.getIsBlindWriter()) {
                    anonymousProfile = anonymousPostProfileRepository.findAnonymousPostProfileByCommunityPostId(post.getId());
                }

                responses.add(InternalLatestPostResponse.of(post, latestActivity, userDetails, categoryName, anonymousProfile));
            });
        }
        return responses;
    }

    private PostWithPoints createPostWithPoints(CommunityPost post) {
        int commentCount = communityCommentRepository.countAllByPostId(post.getId());
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
            anonymousPostProfileService.createAnonymousPostProfile(member, post);
        }
    }

    private void sendSlackNotificationForNonMakers(Member member, CommunityPost post) {
        if (Objects.equals(activeProfile, "prod") && !MakersMemberId.getMakersMember().contains(member.getId())) {
            val slackRequest = createPostSlackRequest(post.getId());
            slackClient.postNotMakersMessage(slackRequest.toString());
        }
    }

    private CommunityPost createCommunityPostBasedOnCategory(Member member, PostSaveRequest request) {
        if (isSopticleCategory(request.categoryId())) {
            SopticleScrapedResponse scrapedResponse = sopticleScrapedService.getSopticleMetaData(request.link());
            PostSaveRequest enrichedRequest = PostSaveRequest.builder()
                    .categoryId(request.categoryId())
                    .content(scrapedResponse.description())
                    .images(new String[] { scrapedResponse.thumbnailUrl() })
                    .link(scrapedResponse.url())
                    .title(scrapedResponse.title())
                    .isBlindWriter(false)
                    .isQuestion(false)
                    .build();
            return communityPostModifier.createCommunityPost(member, enrichedRequest);
        } else {
            return communityPostModifier.createCommunityPost(member, request);
        }
    }

    private boolean isSopticleCategory(Long categoryId) {
        return categoryId == 21;
    }

    private void validatePostOwner(Long memberId, Long postWriterId) {
        if (!Objects.equals(memberId, postWriterId)) {
            throw new ClientBadRequestException("수정/삭제 권한이 없는 유저입니다.");
        }
    }

    private Map<Long, String> getCategoryNameMap(List<CommunityPost> posts) {
        List<Long> categoryIds = posts.stream()
                .map(CommunityPost::getCategoryId)
                .distinct()
                .toList();
        return communityQueryRepository.getCategoryNamesByIds(categoryIds);
    }

    private Map<Long, AnonymousPostProfile> getAnonymousProfileMap(List<CommunityPost> posts) {
        List<Long> postIds = posts.stream()
                .map(CommunityPost::getId)
                .distinct()
                .toList();
        return communityQueryRepository.getAnonymousPostProfilesByPostId(postIds);
    }

    private Map<Long, Category> getCategoryMap(List<CommunityPost> posts) {
        List<Long> categoryIds = posts.stream()
                .map(CommunityPost::getCategoryId)
                .distinct()
                .toList();

        if (categoryIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return categoryRetriever.findAllByIds(categoryIds).stream()
                .collect(toMap(Category::getId, category -> category));
    }
}
