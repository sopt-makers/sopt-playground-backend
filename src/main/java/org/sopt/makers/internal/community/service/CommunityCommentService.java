package org.sopt.makers.internal.community.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.sopt.makers.internal.community.domain.CommunityPost;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousNickname;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousPostProfile;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousProfileImage;
import org.sopt.makers.internal.community.service.comment.CommunityCommentModifier;
import org.sopt.makers.internal.community.service.post.CommunityPostRetriever;
import org.sopt.makers.internal.external.slack.SlackMessageUtil;
import org.sopt.makers.internal.community.service.anonymous.AnonymousNicknameRetriever;
import org.sopt.makers.internal.community.service.anonymous.AnonymousProfileImageRetriever;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousCommentProfile;
import org.sopt.makers.internal.community.domain.comment.CommunityComment;
import org.sopt.makers.internal.community.domain.comment.ReportComment;
import org.sopt.makers.internal.community.dto.CommentDao;
import org.sopt.makers.internal.community.dto.response.CommentListResponse;
import org.sopt.makers.internal.community.dto.request.CommentSaveRequest;
import org.sopt.makers.internal.external.pushNotification.dto.PushNotificationRequest;
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.external.slack.SlackClient;
import org.sopt.makers.internal.community.mapper.CommunityMapper;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.repository.MemberRepository;
import org.sopt.makers.internal.community.repository.anonymous.AnonymousCommentProfileRepository;
import org.sopt.makers.internal.community.repository.anonymous.AnonymousPostProfileRepository;
import org.sopt.makers.internal.community.repository.comment.CommunityCommentRepository;
import org.sopt.makers.internal.community.repository.CommunityQueryRepository;
import org.sopt.makers.internal.community.repository.comment.DeletedCommunityCommentRepository;
import org.sopt.makers.internal.community.repository.comment.ReportCommentRepository;
import org.sopt.makers.internal.internal.InternalApiService;
import org.sopt.makers.internal.external.pushNotification.PushNotificationService;
import org.sopt.makers.internal.member.service.MemberRetriever;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@RequiredArgsConstructor
@Service
@Slf4j
public class CommunityCommentService {

    private final AnonymousProfileImageRetriever anonymousProfileImageRetriever;
    @Value("${spring.profiles.active}")
    private String activeProfile;
    private final DeletedCommunityCommentRepository deletedCommunityCommentRepository;
    private final AnonymousNicknameRetriever anonymousNicknameRetriever;
    private final CommunityMapper communityMapper;
    private final MemberRepository memberRepository;
    private final MemberRetriever memberRetriever;
    private final CommunityCommentModifier communityCommentModifier;
    private final CommunityPostRetriever communityPostRetriever;
    private final CommunityCommentRepository communityCommentsRepository;
    private final ReportCommentRepository reportCommentRepository;
    private final CommunityQueryRepository communityQueryRepository;
    private final AnonymousCommentProfileRepository anonymousCommentProfileRepository;
    private final AnonymousPostProfileRepository anonymousPostProfileRepository;
    private final InternalApiService internalApiService;
    private final PushNotificationService pushNotificationService;
    private final SlackMessageUtil slackMessageUtil;
    private final SlackClient slackClient;

    private final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Transactional
    public void createComment(Long writerId, Long postId, CommentSaveRequest request) {
        Member member = memberRetriever.findMemberById(writerId);
        CommunityPost post = communityPostRetriever.findCommunityPostById(postId);
        CommunityComment comment = communityCommentModifier.createCommunityComment(postId, member.getId(), request);

        if (request.isBlindWriter()) {
            Optional<AnonymousCommentProfile> existingCommentProfile =
                    anonymousCommentProfileRepository.findByMemberIdAndCommunityCommentPostId(member.getId(), post.getId());
            if (existingCommentProfile.isEmpty() ) {
                saveAnonymousProfile(member, post, comment);
            }
        }

        if (!post.getMember().getId().equals(writerId)) {
            sendPushNotification(post, request);
        }
    }

    @Transactional(readOnly = true)
    public List<CommentDao> getPostCommentList(Long postId, Long memberId, Boolean isBlockedOn) {
        communityPostRetriever.checkExistsCommunityPostById(postId);
        return communityQueryRepository.findCommentByPostId(postId, memberId, isBlockedOn);
    }

    @Transactional(readOnly = true)
    public AnonymousCommentProfile getAnonymousCommentProfile(CommunityComment comment) {
        return anonymousCommentProfileRepository.findByMemberIdAndCommunityCommentPostId(comment.getWriterId(), comment.getPostId()).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<CommentListResponse> getCommentList(Long postId) {
        return communityCommentsRepository.findAllByPostId(postId).stream()
                .map(comment -> CommentListResponse.builder()
                        .content(comment.getContent())
                        .parentCommentId(comment.getParentCommentId())
                        .isBlindWriter(comment.getIsBlindWriter())
                        .generation(internalApiService.getMemberLatestActivityGeneration(comment.getWriterId()))
                        .part(internalApiService.getMemberLatestActivityPart(comment.getWriterId()))
                        .createdAt(comment.getCreatedAt())
                        .build()).toList();
    }

    @Transactional
    public void deleteComment(Long commentId, Long writerId) {
        val member = memberRepository.findById(writerId)
                .orElseThrow(() -> new NotFoundDBEntityException("Member"));
        val comment = communityCommentsRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundDBEntityException("CommunityComment"));

        if (!Objects.equals(member.getId(), comment.getWriterId())) {
            throw new ClientBadRequestException("수정 권한이 없는 유저입니다.");
        }

        val deleteComment = communityMapper.toDeleteCommunityComment(comment);
        deletedCommunityCommentRepository.save(deleteComment);
        communityCommentsRepository.delete(comment);
    }

    public void reportComment(Long memberId, Long commentId) {
        val member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundDBEntityException("Is not a Member"));
        val comment = communityCommentsRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundDBEntityException("Is not an exist comment id"));

        try {
            if (Objects.equals(activeProfile, "prod")) {
                val slackRequest = createSlackRequest(comment.getPostId(), member.getName(), comment.getContent());
                slackClient.postReportMessage(slackRequest.toString());
            }
        } catch (RuntimeException ex) {
            log.error("슬랙 요청이 실패했습니다 : " + ex.getMessage());
        }

        reportCommentRepository.save(ReportComment.builder()
                .reporterId(memberId)
                .commentId(commentId)
                .createdAt(LocalDateTime.now(KST))
                .build());
    }

    private JsonNode createSlackRequest(Long id, String name, String comment) {
        val rootNode = slackMessageUtil.getObjectNode();
        rootNode.put("text", "🚨댓글 신고 발생!🚨");

        val blocks = slackMessageUtil.getArrayNode();
        val textField = slackMessageUtil.createTextField("댓글 신고가 들어왔어요!");
        val contentNode = slackMessageUtil.createSection();

        val fields = slackMessageUtil.getArrayNode();
        fields.add(slackMessageUtil.createTextFieldNode("*신고자:*\n" + name));
        fields.add(slackMessageUtil.createTextFieldNode("*댓글 내용:*\n" + comment));
        fields.add(slackMessageUtil.createTextFieldNode("*링크:*\n<https://playground.sopt.org/feed/" + id + "|글>"));
        contentNode.set("fields", fields);

        blocks.add(textField);
        blocks.add(contentNode);
        rootNode.set("blocks", blocks);
        return rootNode;
    }

    private void saveAnonymousProfile(Member member, CommunityPost post, CommunityComment comment) {
        List<AnonymousNickname> excludeNicknames = anonymousCommentProfileRepository.findAllByCommunityCommentPostId(post.getId()).stream()
                .map(AnonymousCommentProfile::getNickname)
                .toList();
        List<Long> excludeImgIds = anonymousCommentProfileRepository.findAllByCommunityCommentPostId(post.getId()).stream()
                .map(p -> p.getProfileImg().getId())
                .toList();
        Optional<AnonymousPostProfile> anonymousPostProfile = anonymousPostProfileRepository.findByMemberAndCommunityPost(member, post);

        AnonymousNickname nickname = anonymousPostProfile.isPresent()
                ? anonymousPostProfile.get().getNickname()
                : anonymousNicknameRetriever.findRandomAnonymousNickname(excludeNicknames);
        AnonymousProfileImage profileImg = anonymousPostProfile.isPresent()
                ? anonymousPostProfile.get().getProfileImg()
                : anonymousProfileImageRetriever.getAnonymousProfileImage(excludeImgIds);

        anonymousCommentProfileRepository.save(
                AnonymousCommentProfile.builder()
                        .nickname(nickname)
                        .profileImg(profileImg)
                        .member(member)
                        .communityComment(comment)
                        .build()
        );
    }

    private void sendPushNotification(CommunityPost post, CommentSaveRequest request) {
        try {
            String title = StringUtils.defaultIfBlank(post.getTitle(),
                    StringUtils.abbreviate(post.getContent(), 20) + "...");
            String message = "\"" + title + "\"" + " 글에 댓글이 달렸어요.";

            PushNotificationRequest pushNotificationRequest = PushNotificationRequest.builder()
                    .title("")
                    .content(message)
                    .category("NEWS")
                    .webLink(request.webLink())
                    .userIds(new String[]{post.getMember().getId().toString()})
                    .build();

            pushNotificationService.sendPushNotification(pushNotificationRequest);
        } catch (Exception error) {
            log.error("Push 알림 실패: {}", error.getMessage());
        }
    }
}
