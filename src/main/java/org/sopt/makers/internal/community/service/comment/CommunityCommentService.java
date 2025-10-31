package org.sopt.makers.internal.community.service.comment;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.sopt.makers.internal.community.domain.CommunityPost;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousProfile;
import org.sopt.makers.internal.community.dto.comment.CommentInfo;
import org.sopt.makers.internal.community.dto.MemberVo;
import org.sopt.makers.internal.community.service.anonymous.AnonymousProfileService;
import org.sopt.makers.internal.community.service.anonymous.AnonymousProfileRetriever;
import org.sopt.makers.internal.community.service.anonymous.AnonymousNicknameRetriever;
import org.sopt.makers.internal.community.service.post.CommunityPostRetriever;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.PlatformService;
import org.sopt.makers.internal.external.slack.SlackMessageUtil;
import org.sopt.makers.internal.external.slack.SlackNotificationService;
import org.sopt.makers.internal.external.slack.message.community.CommentReportSlackMessage;
import org.sopt.makers.internal.external.pushNotification.message.community.CommentNotificationMessage;
import org.sopt.makers.internal.external.pushNotification.message.community.MentionNotificationMessage;
import org.sopt.makers.internal.external.pushNotification.message.community.ReplyNotificationMessage;
import org.sopt.makers.internal.community.domain.comment.CommunityComment;
import org.sopt.makers.internal.community.domain.comment.ReportComment;
import org.sopt.makers.internal.community.dto.comment.CommentDao;
import org.sopt.makers.internal.community.dto.request.comment.CommentSaveRequest;
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.sopt.makers.internal.community.mapper.CommunityMapper;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.community.repository.comment.CommunityCommentRepository;
import org.sopt.makers.internal.community.repository.CommunityQueryRepository;
import org.sopt.makers.internal.community.repository.comment.DeletedCommunityCommentRepository;
import org.sopt.makers.internal.community.repository.comment.ReportCommentRepository;
import org.sopt.makers.internal.external.pushNotification.PushNotificationService;
import org.sopt.makers.internal.member.service.MemberRetriever;
import org.sopt.makers.internal.member.service.career.MemberCareerRetriever;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@RequiredArgsConstructor
@Service
@Slf4j
public class CommunityCommentService {

    private final PlatformService platformService;
    private final MemberCareerRetriever memberCareerRetriever;

    private final DeletedCommunityCommentRepository deletedCommunityCommentRepository;
    private final CommunityCommentRepository communityCommentsRepository;
    private final ReportCommentRepository reportCommentRepository;
    private final CommunityQueryRepository communityQueryRepository;

    private final AnonymousProfileService anonymousProfileService;
    private final AnonymousProfileRetriever anonymousProfileRetriever;
    private final AnonymousNicknameRetriever anonymousNicknameRetriever;
    private final CommunityPostRetriever communityPostRetriever;
    private final CommunityCommentsRetriever communityCommentsRetriever;
    private final MemberRetriever memberRetriever;

    private final CommunityCommentsModifier communityCommentsModifier;

    private final CommunityMapper communityMapper;

    private final PushNotificationService pushNotificationService;
    private final SlackMessageUtil slackMessageUtil;
    private final SlackNotificationService slackNotificationService;

    private final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Transactional
    public void createComment(Long writerId, Long postId, CommentSaveRequest request) {
        Member member = memberRetriever.findMemberById(writerId);
        CommunityPost post = communityPostRetriever.findCommunityPostById(postId);

        if (request.isChildComment()) {
            communityCommentsRetriever.checkExistsCommunityCommentById(request.parentCommentId());
            validateAnonymousNickname(request, postId);
        }

        InternalUserDetails writerDetails = platformService.getInternalUser(writerId);
        CommunityComment comment = communityCommentsModifier.createCommunityComment(postId, member.getId(), request);

        if (request.isBlindWriter()) {
            AnonymousProfile profile = anonymousProfileService.getOrCreateAnonymousProfile(member, post);
            comment.registerAnonymousProfile(profile);
            communityCommentsRepository.save(comment);
        }

        // 푸시 알림 전송
        sendNotifications(writerId, post, request, writerDetails.name());
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
    public AnonymousProfile getAnonymousCommentProfile(CommunityComment comment) {
        return comment.getAnonymousProfile();
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

        // 슬랙 알림 전송 (SOLID 원칙 적용)
        CommentReportSlackMessage slackMessage = CommentReportSlackMessage.of(
                slackMessageUtil,
                comment.getPostId(),
                userDetails.name(),
                comment.getContent()
        );
        slackNotificationService.sendCommentReport(slackMessage);

        reportCommentRepository.save(ReportComment.builder()
                .reporterId(memberId)
                .commentId(commentId)
                .createdAt(LocalDateTime.now(KST))
                .build());
    }


    private void validateAnonymousNickname(CommentSaveRequest request, Long postId) {
        if (
                request.anonymousMentionRequest() == null
                || request.anonymousMentionRequest().anonymousNicknames() == null
                || request.anonymousMentionRequest().anonymousNicknames().length == 0
        ) {
            return;
        }

        String[] anonymousNicknames = request.anonymousMentionRequest().anonymousNicknames();

        // 1. 익명 닉네임이 시스템에 존재하는지 검증
        anonymousNicknameRetriever.validateAnonymousNicknames(anonymousNicknames);

        // 2. 해당 게시글에 존재하는 닉네임인지 검증
        List<String> nicknameList = Arrays.asList(anonymousNicknames);
        List<String> foundNicknames = anonymousProfileRetriever.findNicknamesByPostIdAndNicknamesIn(postId, nicknameList);
        Set<String> foundNicknameSet = Set.copyOf(foundNicknames);

        for (String nickname : anonymousNicknames) {
            if (!foundNicknameSet.contains(nickname)) {
                throw new ClientBadRequestException("해당 게시글에 존재하지 않는 익명 닉네임입니다: " + nickname);
            }
        }
    }

    private void sendNotifications(Long writerId, CommunityPost post, CommentSaveRequest request, String writerName) {
        if (!post.getMember().getId().equals(writerId)) {
            CommentNotificationMessage message = CommentNotificationMessage.of(
                    post.getMember().getId(),
                    writerName,
                    request.content(),
                    request.isBlindWriter(),
                    request.webLink()
            );
            pushNotificationService.sendPushNotification(message);
        }

        if (request.isChildComment()) {
            CommunityComment parentComment = communityCommentsRetriever.findCommunityCommentById(request.parentCommentId());
            Long parentCommentAuthorId = parentComment.getWriterId();

            if (!parentCommentAuthorId.equals(writerId) && !parentCommentAuthorId.equals(post.getMember().getId())) {
                ReplyNotificationMessage message = ReplyNotificationMessage.of(
                        parentCommentAuthorId,
                        writerName,
                        request.content(),
                        request.isBlindWriter(),
                        request.webLink()
                );
                pushNotificationService.sendPushNotification(message);
            }
        }

        if (Objects.nonNull(request.mention())) {
            MentionNotificationMessage message = MentionNotificationMessage.of(
                    request.mention().userIds(),
                    writerName,
                    request.content(),
                    request.isBlindWriter(),
                    request.webLink()
            );
            pushNotificationService.sendPushNotification(message);
        }
    }
}
