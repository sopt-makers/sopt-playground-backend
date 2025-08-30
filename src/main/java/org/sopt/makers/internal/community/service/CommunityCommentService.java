package org.sopt.makers.internal.community.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.sopt.makers.internal.community.domain.CommunityPost;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousNickname;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousPostProfile;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousProfileImage;
import org.sopt.makers.internal.community.dto.CommentInfo;
import org.sopt.makers.internal.community.dto.MemberVo;
import org.sopt.makers.internal.community.service.comment.CommunityCommentModifier;
import org.sopt.makers.internal.community.service.comment.CommunityCommentsRetriever;
import org.sopt.makers.internal.community.service.post.CommunityPostRetriever;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.PlatformService;
import org.sopt.makers.internal.external.slack.SlackMessageUtil;
import org.sopt.makers.internal.community.service.anonymous.AnonymousNicknameRetriever;
import org.sopt.makers.internal.community.service.anonymous.AnonymousProfileImageRetriever;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousCommentProfile;
import org.sopt.makers.internal.community.domain.comment.CommunityComment;
import org.sopt.makers.internal.community.domain.comment.ReportComment;
import org.sopt.makers.internal.community.dto.CommentDao;
import org.sopt.makers.internal.community.dto.request.CommentSaveRequest;
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.sopt.makers.internal.external.slack.SlackClient;
import org.sopt.makers.internal.community.mapper.CommunityMapper;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.community.repository.anonymous.AnonymousCommentProfileRepository;
import org.sopt.makers.internal.community.repository.anonymous.AnonymousPostProfileRepository;
import org.sopt.makers.internal.community.repository.comment.CommunityCommentRepository;
import org.sopt.makers.internal.community.repository.CommunityQueryRepository;
import org.sopt.makers.internal.community.repository.comment.DeletedCommunityCommentRepository;
import org.sopt.makers.internal.community.repository.comment.ReportCommentRepository;
import org.sopt.makers.internal.external.pushNotification.PushNotificationService;
import org.sopt.makers.internal.member.service.MemberRetriever;
import org.sopt.makers.internal.common.util.MentionCleaner;
import org.sopt.makers.internal.member.service.career.MemberCareerRetriever;
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

    @Value("${spring.profiles.active}")
    private String activeProfile;

    private final PlatformService platformService;
    private final MemberCareerRetriever memberCareerRetriever;

    private final AnonymousCommentProfileRepository anonymousCommentProfileRepository;
    private final AnonymousPostProfileRepository anonymousPostProfileRepository;
    private final DeletedCommunityCommentRepository deletedCommunityCommentRepository;
    private final CommunityCommentRepository communityCommentsRepository;
    private final ReportCommentRepository reportCommentRepository;
    private final CommunityQueryRepository communityQueryRepository;

    private final AnonymousProfileImageRetriever anonymousProfileImageRetriever;
    private final AnonymousNicknameRetriever anonymousNicknameRetriever;
    private final CommunityPostRetriever communityPostRetriever;
    private final CommunityCommentsRetriever communityCommentsRetriever;
    private final MemberRetriever memberRetriever;

    private final CommunityCommentModifier communityCommentModifier;

    private final CommunityMapper communityMapper;

    private final PushNotificationService pushNotificationService;
    private final SlackMessageUtil slackMessageUtil;
    private final SlackClient slackClient;

    private final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Transactional
    public void createComment(Long writerId, Long postId, CommentSaveRequest request) {
        Member member = memberRetriever.findMemberById(writerId);
        InternalUserDetails writerDetails = platformService.getInternalUser(writerId);
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
            sendCommentPushNotification(post.getMember().getId(), request, writerDetails.name());
        }

        if(Objects.nonNull(request.mention())) {
            sendMentionPushNotification(request.content(), request);
        }
    }

    @Transactional(readOnly = true)
    public List<CommentInfo> getPostCommentList(Long postId, Long memberId, Boolean isBlockedOn) {
        communityPostRetriever.checkExistsCommunityPostById(postId);
        List<CommentDao> commentDaos = communityQueryRepository.findCommentByPostId(postId, memberId, isBlockedOn);

        return commentDaos.stream().map(dao -> {
            val authorDetails = platformService.getInternalUser(dao.member().getId());
            val authorCareer = memberCareerRetriever.findMemberLastCareerByMemberId(dao.member().getId());
            val memberVo = MemberVo.of(authorDetails, authorCareer);
            val anonymousProfile = getAnonymousCommentProfile(dao.comment());
            return new CommentInfo(dao, memberVo, anonymousProfile);
        }).toList();
    }

    @Transactional(readOnly = true)
    public AnonymousCommentProfile getAnonymousCommentProfile(CommunityComment comment) {
        return anonymousCommentProfileRepository.findByCommunityCommentId(comment.getId()).orElse(null);
    }

    @Transactional
    public void deleteComment(Long commentId, Long writerId) {
        CommunityComment comment = communityCommentsRetriever.findCommunityCommentById(commentId);

        if (!Objects.equals(writerId, comment.getWriterId())) {
            throw new ClientBadRequestException("수정 권한이 없는 유저입니다.");
        }

        val deleteComment = communityMapper.toDeleteCommunityComment(comment);
        deletedCommunityCommentRepository.save(deleteComment);
        communityCommentsRepository.delete(comment);
    }

    public void reportComment(Long memberId, Long commentId) {
        CommunityComment comment = communityCommentsRetriever.findCommunityCommentById(commentId);
        InternalUserDetails userDetails = platformService.getInternalUser(memberId);

        try {
            if (Objects.equals(activeProfile, "prod")) {
                val slackRequest = createSlackRequest(comment.getPostId(), userDetails.name(), comment.getContent());
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
        List<AnonymousNickname> excludeNicknames = new ArrayList<>();
        for (AnonymousCommentProfile profile : anonymousCommentProfileRepository.findAllByCommunityCommentPostId(post.getId())) {
            excludeNicknames.add(profile.getNickname());
        }

        Optional<AnonymousPostProfile> anonymousPostProfile = anonymousPostProfileRepository.findByMemberAndCommunityPost(member, post);

        AnonymousNickname nickname = anonymousPostProfile.isPresent()
                ? anonymousPostProfile.get().getNickname()
                : anonymousNicknameRetriever.findRandomAnonymousNickname(excludeNicknames);
        AnonymousProfileImage profileImg = anonymousPostProfile.isPresent()
                ? anonymousPostProfile.get().getProfileImg()
                : anonymousProfileImageRetriever.getAnonymousProfileImage();

        anonymousCommentProfileRepository.save(
                AnonymousCommentProfile.builder()
                        .nickname(nickname)
                        .profileImg(profileImg)
                        .member(member)
                        .communityComment(comment)
                        .build()
        );
    }

    private void sendCommentPushNotification(Long userId, CommentSaveRequest request, String commentWriterName) {
        String title = "💬나의 게시글에 새로운 댓글이 달렸어요.";
        String writerName = request.isBlindWriter() ? "익명" : commentWriterName;
        String content = "[" + writerName + "의 댓글] : \""
                + StringUtils.abbreviate(MentionCleaner.removeMentionIds(request.content()), 100) + "\"";
        Long[] userIds = new Long[]{userId};

        pushNotificationService.sendPushNotification(title, content, userIds, request.webLink());
    }

    private void sendMentionPushNotification(String commentContent, CommentSaveRequest request) {
        String writerName = request.isBlindWriter() ? "익명" : request.mention().writerName();
        String title = "💬" + writerName + "님이 회원님을 언급했어요.";
        String content = "\"" + StringUtils.abbreviate(MentionCleaner.removeMentionIds(commentContent), 100) + "\"";

        pushNotificationService.sendPushNotification(title, content, request.mention().userIds(), request.webLink());
    }
}
