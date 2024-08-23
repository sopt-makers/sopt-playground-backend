package org.sopt.makers.internal.community.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.sopt.makers.internal.common.SlackMessageUtil;
import org.sopt.makers.internal.domain.community.AnonymousCommentProfile;
import org.sopt.makers.internal.domain.community.CommunityComment;
import org.sopt.makers.internal.domain.community.ReportComment;
import org.sopt.makers.internal.dto.community.CommentDao;
import org.sopt.makers.internal.dto.community.CommentListResponse;
import org.sopt.makers.internal.dto.community.CommentSaveRequest;
import org.sopt.makers.internal.dto.pushNotification.PushNotificationRequest;
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.external.slack.SlackClient;
import org.sopt.makers.internal.mapper.CommunityMapper;
import org.sopt.makers.internal.repository.MemberRepository;
import org.sopt.makers.internal.repository.community.AnonymousCommentProfileRepository;
import org.sopt.makers.internal.repository.community.AnonymousNicknameRepository;
import org.sopt.makers.internal.repository.community.AnonymousPostProfileRepository;
import org.sopt.makers.internal.repository.community.CommunityCommentRepository;
import org.sopt.makers.internal.repository.community.CommunityPostRepository;
import org.sopt.makers.internal.repository.community.CommunityQueryRepository;
import org.sopt.makers.internal.repository.community.DeletedCommunityCommentRepository;
import org.sopt.makers.internal.repository.community.ReportCommentRepository;
import org.sopt.makers.internal.service.InternalApiService;
import org.sopt.makers.internal.service.PushNotificationService;
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
    private final DeletedCommunityCommentRepository deletedCommunityCommentRepository;
    private final CommunityMapper communityMapper;
    private final MemberRepository memberRepository;
    private final CommunityPostRepository communityPostRepository;
    private final CommunityCommentRepository communityCommentsRepository;
    private final ReportCommentRepository reportCommentRepository;
    private final CommunityQueryRepository communityQueryRepository;
    private final AnonymousCommentProfileRepository anonymousCommentProfileRepository;
    private final AnonymousPostProfileRepository anonymousPostProfileRepository;
    private final AnonymousNicknameRepository anonymousNicknameRepository;
    private final InternalApiService internalApiService;
    private final AnonymousProfileImageService anonymousProfileImageService;
    private final PushNotificationService pushNotificationService;
    private final SlackMessageUtil slackMessageUtil;
    private final SlackClient slackClient;

    private final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Transactional
    public void createComment(Long writerId, Long postId, CommentSaveRequest request) {
        val member = memberRepository.findById(writerId)
                .orElseThrow(() -> new NotFoundDBEntityException("Member"));

        val post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundDBEntityException("Community Post"));

        if (request.isChildComment() && !communityCommentsRepository.existsById(request.parentCommentId())) {
            throw new NotFoundDBEntityException("CommunityComment");
        }

        val excludeImgList = anonymousCommentProfileRepository.findAllByCommunityCommentPostId(postId).stream()
            .map(p -> p.getProfileImg().getId()).toList();
        val excludeNickname = anonymousCommentProfileRepository.findAllByCommunityCommentPostId(postId).stream()
            .map(AnonymousCommentProfile::getNickname).toList();
        val anonymousPostProfile = anonymousPostProfileRepository.findByMemberAndCommunityPost(member, post);
        val anonymousCommentProfile = anonymousCommentProfileRepository.findByMemberIdAndCommunityCommentPostId(writerId, postId);

        CommunityComment comment = CommunityComment.builder()
            .content(request.content())
            .postId(postId)
            .writerId(member.getId())
            .parentCommentId(request.parentCommentId())
            .isBlindWriter(request.isBlindWriter())
            .build();
        communityCommentsRepository.save(comment);

        if (request.isBlindWriter() && anonymousCommentProfile.isEmpty()) {
            anonymousCommentProfileRepository.save(AnonymousCommentProfile.builder()
                .nickname(member.equals(post.getMember()) ? anonymousPostProfile.get().getNickname() : AnonymousNicknameServiceUtil.getRandomNickname(anonymousNicknameRepository, excludeNickname))
                .profileImg(member.equals(post.getMember()) ? anonymousPostProfile.get().getProfileImg() : anonymousProfileImageService.getRandomProfileImage(excludeImgList))
                .member(member)
                .communityComment(comment)
                .build());
        }

        // 본인 게시글의 본인 댓글에는 알림이 가지 않음
        if (post.getMember().getId().equals(writerId)) return;

        try {
            String title = post.getTitle();
            String content = post.getContent();

            if (StringUtils.isBlank(title)) {
                title = StringUtils.abbreviate(content, 20) + "...";
            }
            String pushNotificationContent = "\"" + title + "\"" + " 글에 댓글이 달렸어요.";

            PushNotificationRequest pushNotificationRequest = PushNotificationRequest.builder()
                    .title("")
                    .content(pushNotificationContent)
                    .category("NEWS")
                    .webLink(request.webLink())
                    .userIds(new String[]{post.getMember().getId().toString()})
                    .build();

            pushNotificationService.sendPushNotification(pushNotificationRequest);
        } catch (Exception error) {
            log.error(error.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<CommentDao> getPostCommentList(Long postId) {
        val post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundDBEntityException("Is not a categoryId"));
        return communityQueryRepository.findCommentByPostId(postId);
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
}
