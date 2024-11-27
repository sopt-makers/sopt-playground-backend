package org.sopt.makers.internal.community.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.sopt.makers.internal.common.MakersMemberId;
import org.sopt.makers.internal.common.SlackMessageUtil;
import org.sopt.makers.internal.community.controller.dto.request.PostSaveRequest;
import org.sopt.makers.internal.community.domain.AnonymousProfileImage;
import org.sopt.makers.internal.community.domain.CommunityPostLike;
import org.sopt.makers.internal.community.repository.CommunityPostLikeRepository;
import org.sopt.makers.internal.community.repository.CommunityPostRepository;
import org.sopt.makers.internal.community.repository.category.CategoryRepository;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.community.*;
import org.sopt.makers.internal.dto.community.*;
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.external.slack.SlackClient;
import org.sopt.makers.internal.mapper.CommunityMapper;
import org.sopt.makers.internal.mapper.CommunityResponseMapper;
import org.sopt.makers.internal.repository.MemberRepository;
import org.sopt.makers.internal.repository.PostRepository;
import org.sopt.makers.internal.repository.community.*;
import org.sopt.makers.internal.repository.member.MemberBlockRepository;
import org.sopt.makers.internal.service.member.MemberServiceUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommunityPostService {

    private final CommunityCommentRepository communityCommentRepository;
    private final CommunityPostLikeRepository communityPostLikeRepository;
    private final CommunityPostRepository communityPostRepository;

    private final CommunityQueryRepository communityQueryRepository;

    private final DeletedCommunityPostRepository deletedCommunityPostRepository;
    private final DeletedCommunityCommentRepository deletedCommunityCommentRepository;

    private final ReportPostRepository reportPostRepository;
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;
    private final MemberBlockRepository memberBlockRepository;

    private final AnonymousProfileImageService anonymousProfileImageService;
    private final AnonymousPostProfileRepository anonymousPostProfileRepository;
    private final AnonymousNicknameRepository anonymousNicknameRepository;

    private final CommunityMapper communityMapper;
    private final CommunityResponseMapper communityResponseMapper;

    private final SlackMessageUtil slackMessageUtil;
    private final SlackClient slackClient;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    private final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int MIN_POINTS_FOR_HOT_POST = 10;

    @Transactional(readOnly = true)
    public List<CommunityPostMemberVo> getAllPosts(Long categoryId, Boolean isBlockedOn, Long memberId, Integer limit, Long cursor) {
        if (limit == null || limit >= 50) limit = 50;
        if (categoryId == null) {
            val posts = communityQueryRepository.findAllPostByCursor(limit, cursor, memberId, isBlockedOn);
            return posts.stream().map(communityResponseMapper::toCommunityVo).collect(Collectors.toList());
        } else {
            categoryRepository.findById(categoryId).orElseThrow(
                    () -> new ClientBadRequestException("존재하지 않는 categoryId입니다."));
            val posts = communityQueryRepository.findAllParentCategoryPostByCursor(categoryId, limit, cursor, memberId, isBlockedOn);
            return posts.stream().map(communityResponseMapper::toCommunityVo).collect(Collectors.toList());
        }
    }

    @Transactional(readOnly = true)
    public CommunityPostMemberVo getPostById(Long memberId, Long postId, Boolean isBlockedOn) {
        val postDao = communityQueryRepository.getPostById(postId);
        if (Objects.isNull(postDao)) throw new ClientBadRequestException("존재하지 않는 postId입니다.");

        val blocker = MemberServiceUtil.findMemberById(memberRepository, memberId);
        val blockedMember = MemberServiceUtil.findMemberById(memberRepository, postDao.member().getId());

        if (isBlockedOn && memberBlockRepository.existsByBlockerAndBlockedMember(blocker, blockedMember)) {
            MemberServiceUtil.checkBlockedMember(memberBlockRepository, blocker, blockedMember);
        }

        return communityResponseMapper.toCommunityVo(postDao);
    }

    @Transactional
    public PostSaveResponse createPost(Long writerId, PostSaveRequest request) {
        Member member = MemberServiceUtil.findMemberById(memberRepository, writerId);
        CommunityPost post = communityPostRepository.save(CommunityPost.builder()
                .member(member)
                .categoryId(request.categoryId())
                .title(request.title())
                .content(request.content())
                .hits(0)
                .images(request.images())
                .isQuestion(request.isQuestion())
                .isBlindWriter(request.isBlindWriter())
                .comments(new ArrayList<>())
                .build());

        if (request.isBlindWriter()) {
            List<AnonymousPostProfile> lastFourAnonymousPostProfiles = anonymousPostProfileRepository.findTop4ByOrderByCreatedAtDesc();
            List<AnonymousPostProfile> lastFiftyAnonymousNickname = anonymousPostProfileRepository.findTop50ByOrderByCreatedAtDesc();
            List<Long> usedAnonymousProfileImages = lastFourAnonymousPostProfiles.stream()
                    .map(anonymousProfile -> anonymousProfile.getProfileImg().getId()).toList();
            List<AnonymousNickname> usedAnonymousNicknames = lastFiftyAnonymousNickname.stream()
                    .map(AnonymousPostProfile::getNickname).toList();

            AnonymousProfileImage anonymousProfileImg = anonymousProfileImageService.getRandomProfileImage(usedAnonymousProfileImages);
            AnonymousNickname anonymousNickname = AnonymousNicknameServiceUtil.getRandomNickname(anonymousNicknameRepository, usedAnonymousNicknames);
            anonymousPostProfileRepository.save(AnonymousPostProfile.builder()
                    .member(member)
                    .nickname(anonymousNickname)
                    .profileImg(anonymousProfileImg)
                    .communityPost(post)
                    .build()
            );
        }

        if (!MakersMemberId.getMakersMember().contains(member.getId()) && Objects.equals(activeProfile, "prod")) {
            val slackRequest = createPostSlackRequest(post.getId());
            slackClient.postNotMakersMessage(slackRequest.toString());
        }

        return communityResponseMapper.toPostSaveResponse(post);
    }

    @Transactional
    public PostUpdateResponse updatePost(Long writerId, PostUpdateRequest request) {
        val member = memberRepository.findById(writerId)
                .orElseThrow(() -> new NotFoundDBEntityException("Is not a Member"));
        val post = postRepository.findById(request.postId()).orElseThrow(
                () -> new NotFoundDBEntityException("Is not a exist postId"));
        val category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NotFoundDBEntityException("Is not a categoryId"));

        if (!Objects.equals(member.getId(), post.getMember().getId())) {
            throw new ClientBadRequestException("수정 권한이 없는 유저입니다.");
        }

        communityPostRepository.save(CommunityPost.builder()
                .id(request.postId())
                .member(member)
                .categoryId(request.categoryId())
                .title(request.title())
                .content(request.content())
                .hits(post.getHits())
                .images(request.images())
                .isQuestion(request.isQuestion())
                .isBlindWriter(request.isBlindWriter())
                .comments(communityCommentRepository.findAllByPostId(request.postId()))
                .build());
        return communityResponseMapper.toPostUpdateResponse(post);
    }

    @Transactional
    public void deletePost(Long postId, Long writerId) {
        val member = memberRepository.findById(writerId)
                .orElseThrow(() -> new NotFoundDBEntityException("Is not a Member"));
        val post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundDBEntityException("Is not a categoryId"));

        if (!Objects.equals(member.getId(), post.getMember().getId())) {
            throw new ClientBadRequestException("삭제 권한이 없는 유저입니다.");
        }

        val deletedPost = communityMapper.toDeleteCommunityPost(post);
        deletedCommunityPostRepository.save(deletedPost);
        post.getComments().stream().map(communityMapper::toDeleteCommunityComment)
                .forEach(deletedCommunityCommentRepository::save);
        communityPostRepository.delete(post);
    }

    @Transactional
    public void increaseHit(List<Long> postIdLists) {
        for (Long id : postIdLists) {
            val post = postRepository.findById(id)
                    .orElseThrow(() -> new NotFoundDBEntityException("Is not an exist post id"));
        }

        communityQueryRepository.updateHitsByPostId(postIdLists);
    }

    @Transactional
    public void reportPost(Long memberId, Long postId) {
        val member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundDBEntityException("Is not a Member"));
        val post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundDBEntityException("Is not an exist post id"));

        try {
            if (Objects.equals(activeProfile, "prod")) {
                val slackRequest = createReportSlackRequest(post.getId(), member.getName());
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

        Member member = MemberServiceUtil.findMemberById(memberRepository, memberId);
        CommunityPost post = CommunityPostServiceUtil.findCommunityPostById(communityPostRepository, postId);

        if (CommunityPostServiceUtil.isAlreadyLikePost(communityPostLikeRepository, memberId, postId)) {
            throw new ClientBadRequestException("이미 좋아요를 누른 게시물입니다.");
        }

        communityPostLikeRepository.save(CommunityPostLike.builder().member(member).post(post).build());
    }

    @Transactional
    public void unlikePost(Long memberId, Long postId) {

        MemberServiceUtil.checkExistsMemberById(memberRepository, memberId);
        CommunityPostServiceUtil.checkExistsCommunityPostById(communityPostRepository, postId);

        if (!CommunityPostServiceUtil.isAlreadyLikePost(communityPostLikeRepository, memberId, postId)) {
            throw new ClientBadRequestException("좋아요를 누른적이 없는 게시물입니다.");
        }

        CommunityPostLike communityPostLike = communityPostLikeRepository.findCommunityPostLikeByMemberIdAndPostId(memberId, postId)
                .orElseThrow(() -> new NotFoundDBEntityException("좋아요를 누른적이 없는 게시물입니다"));

        communityPostLikeRepository.delete(communityPostLike);
    }

    @Transactional(readOnly = true)
    public Boolean isLiked(Long memberId, Long postId) {

        MemberServiceUtil.checkExistsMemberById(memberRepository, memberId);
        CommunityPostServiceUtil.checkExistsCommunityPostById(communityPostRepository, postId);

        return CommunityPostServiceUtil.isAlreadyLikePost(communityPostLikeRepository, memberId, postId);
    }

    @Transactional(readOnly = true)
    public Integer getLikes(Long postId) {
        CommunityPostServiceUtil.checkExistsCommunityPostById(communityPostRepository, postId);
        return communityPostLikeRepository.countAllByPostId(postId);
    }

    @Transactional(readOnly = true)
    public AnonymousPostProfile getAnonymousPostProfile(Long memberId, Long postId) {
        return anonymousPostProfileRepository.findAnonymousPostProfileByMemberIdAndCommunityPostId(memberId, postId).orElse(null);
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

    private record PostWithPoints(CommunityPost post, int points, int hits) {}
}
