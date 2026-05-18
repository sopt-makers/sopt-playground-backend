package org.sopt.makers.internal.community.service.comment;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.sopt.makers.internal.community.domain.CommunityPost;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousProfile;
import org.sopt.makers.internal.community.domain.comment.DeletedCommunityComment;
import org.sopt.makers.internal.community.dto.comment.CommentInfo;
import org.sopt.makers.internal.community.dto.MemberVo;
import org.sopt.makers.internal.community.dto.request.comment.CommentUpdateRequest;
import org.sopt.makers.internal.community.service.anonymous.AnonymousProfileService;
import org.sopt.makers.internal.community.service.anonymous.AnonymousProfileRetriever;
import org.sopt.makers.internal.community.service.anonymous.AnonymousNicknameRetriever;
import org.sopt.makers.internal.community.service.member.CommunityMemberVoAssembler;
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
import org.sopt.makers.internal.exception.BadRequestException;
import org.sopt.makers.internal.community.mapper.CommunityMapper;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.community.repository.comment.CommunityCommentRepository;
import org.sopt.makers.internal.community.repository.CommunityQueryRepository;
import org.sopt.makers.internal.community.repository.comment.DeletedCommunityCommentRepository;
import org.sopt.makers.internal.community.repository.comment.ReportCommentRepository;
import org.sopt.makers.internal.common.event.PushNotificationEvent;
import org.sopt.makers.internal.member.service.MemberRetriever;
import org.springframework.context.ApplicationEventPublisher;
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
    private final CommunityMemberVoAssembler communityMemberVoAssembler;

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
    private final CommentMentionAnonymizer commentMentionAnonymizer;

    private final CommunityMapper communityMapper;

    private final ApplicationEventPublisher eventPublisher;
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

        return getPostCommentMap(List.of(postId), memberId, isBlockedOn)
            .getOrDefault(postId, List.of());
    }

    @Transactional(readOnly = true)
    public Map<Long, List<CommentInfo>> getPostCommentMap(
        List<Long> postIds,
        Long memberId,
        Boolean isBlockedOn
    ) {
        if (postIds == null || postIds.isEmpty()) {
            return Map.of();
        }

        List<CommentDao> commentDaos = communityQueryRepository.findCommentsByPostIds(
            postIds,
            memberId,
            Boolean.TRUE.equals(isBlockedOn)
        );

        List<Long> commentWriterIds = commentDaos.stream()
            .map(commentDao -> commentDao.member().getId())
            .distinct()
            .toList();

        Map<Long, MemberVo> memberVoMap = communityMemberVoAssembler.getMemberVoMap(commentWriterIds);

        Map<Long, List<CommentInfo>> commentInfoMap = commentDaos.stream()
            .map(commentDao -> {
                Long commentWriterId = commentDao.member().getId();

                return new CommentInfo(
                    commentDao,
                    memberVoMap.get(commentWriterId),
                    getAnonymousCommentProfile(commentDao.comment())
                );
            })
            .collect(Collectors.groupingBy(
                commentInfo -> commentInfo.commentDao().comment().getPostId()
            ));

        return postIds.stream()
            .collect(Collectors.toMap(
                postId -> postId,
                postId -> commentInfoMap.getOrDefault(postId, List.of())
            ));
    }

    @Transactional(readOnly = true)
    public AnonymousProfile getAnonymousCommentProfile(CommunityComment comment) {
        return comment.getAnonymousProfile();
    }

    @Transactional
    public void deleteComment(Long commentId, Long writerId) {
        CommunityComment comment = communityCommentsRetriever.findCommunityCommentById(commentId);

        if (!Objects.equals(writerId, comment.getWriterId())) {
            throw new BadRequestException("수정 권한이 없는 유저입니다.");
        }

        commentMentionAnonymizer.anonymizeMentionsInReplies(comment);
        DeletedCommunityComment deleteComment = communityMapper.toDeleteCommunityComment(comment);
        deletedCommunityCommentRepository.save(deleteComment);

        comment.markAsDeleted();
        communityCommentsRepository.save(comment);
    }

    @Transactional
    public void reportComment(Long memberId, Long commentId) {
        CommunityComment comment = communityCommentsRetriever.findCommunityCommentById(commentId);
        InternalUserDetails userDetails = platformService.getInternalUser(memberId);

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

    @Transactional
    public void updateComment(Long memberId, Long commentId, CommentUpdateRequest request) {
        memberRetriever.checkExistsMemberById(memberId);
        CommunityComment comment = communityCommentsRetriever.findCommunityCommentById(commentId);
        comment.validateUpdatePermission(memberId);

        communityCommentsModifier.updateCommunityComment(comment, request);
    }

    private void validateAnonymousNickname(CommentSaveRequest request, Long postId) {
        if (
                request.anonymousMention() == null
                        || request.anonymousMention().anonymousNicknames() == null
                        || request.anonymousMention().anonymousNicknames().length == 0
        ) {
            return;
        }

        String[] anonymousNicknames = request.anonymousMention().anonymousNicknames();

        // 1. 익명 닉네임이 시스템에 존재하는지 검증
        anonymousNicknameRetriever.validateAnonymousNicknames(anonymousNicknames);

        // 2. 해당 게시글에 존재하는 닉네임인지 검증
        List<String> nicknameList = Arrays.asList(anonymousNicknames);
        List<String> foundNicknames = anonymousProfileRetriever.findNicknamesByPostIdAndNicknamesIn(postId, nicknameList);
        Set<String> foundNicknameSet = Set.copyOf(foundNicknames);

        for (String nickname : anonymousNicknames) {
            if (!foundNicknameSet.contains(nickname)) {
                throw new BadRequestException("해당 게시글에 존재하지 않는 익명 닉네임입니다: " + nickname);
            }
        }
    }

    private void sendNotifications(Long writerId, CommunityPost post, CommentSaveRequest request, String writerName) {
        Long postAuthorId = post.getMember().getId();

        if (!postAuthorId.equals(writerId)) {
            CommentNotificationMessage message = CommentNotificationMessage.of(
                    postAuthorId,
                    writerName,
                    request.content(),
                    request.isBlindWriter(),
                    request.webLink()
            );
            eventPublisher.publishEvent(PushNotificationEvent.of(message));
        }

        if (request.isChildComment()) {
            CommunityComment parentComment = communityCommentsRetriever.findCommunityCommentById(request.parentCommentId());
            Long parentCommentAuthorId = parentComment.getWriterId();

            if (!parentCommentAuthorId.equals(writerId) && !parentCommentAuthorId.equals(postAuthorId)) {
                ReplyNotificationMessage message = ReplyNotificationMessage.of(
                        parentCommentAuthorId,
                        writerName,
                        request.content(),
                        request.isBlindWriter(),
                        request.webLink()
                );
                eventPublisher.publishEvent(PushNotificationEvent.of(message));
            }
        }

        if (Objects.nonNull(request.mention())) {
            sendMentionNotifications(
                    writerId,
                    request.mention().userIds(),
                    writerName,
                    request.content(),
                    request.isBlindWriter(),
                    request.webLink()
            );
        }
    }

    private void sendMentionNotifications(
            Long writerId,
            Long[] recipientIds,
            String writerName,
            String content,
            Boolean isBlindWriter,
            String webLink
    ) {
        if (recipientIds == null || recipientIds.length == 0) {
            return;
        }

        Long[] filteredRecipientIds = Arrays.stream(recipientIds)
                .filter(recipientId -> !recipientId.equals(writerId))
                .toArray(Long[]::new);

        if (filteredRecipientIds.length == 0) {
            return;
        }

        MentionNotificationMessage message = MentionNotificationMessage.of(
                filteredRecipientIds,
                writerName,
                content,
                isBlindWriter,
                webLink
        );
        eventPublisher.publishEvent(PushNotificationEvent.of(message));
    }

}
