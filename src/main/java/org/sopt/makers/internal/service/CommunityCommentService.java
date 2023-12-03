package org.sopt.makers.internal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.sopt.makers.internal.domain.community.CommunityComment;
import org.sopt.makers.internal.domain.community.ReportComment;
import org.sopt.makers.internal.dto.community.CommentDao;
import org.sopt.makers.internal.dto.community.CommentListResponse;
import org.sopt.makers.internal.dto.community.CommentSaveRequest;
import org.sopt.makers.internal.dto.pushNotification.PushNotificationRequest;
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.mapper.CommunityMapper;
import org.sopt.makers.internal.repository.community.*;
import org.sopt.makers.internal.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
@Slf4j
public class CommunityCommentService {
    private final DeletedCommunityCommentRepository deletedCommunityCommentRepository;
    private final CommunityMapper communityMapper;
    private final MemberRepository memberRepository;
    private final CommunityPostRepository communityPostRepository;
    private final CommunityCommentRepository communityCommentsRepository;
    private final ReportCommentRepository reportCommentRepository;
    private final CommunityQueryRepository communityQueryRepository;
    private final InternalApiService internalApiService;
    private final PushNotificationService pushNotificationService;

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

        communityCommentsRepository.save(CommunityComment.builder()
                        .content(request.content())
                        .postId(postId)
                        .writerId(member.getId())
                        .parentCommentId(request.parentCommentId())
                        .isBlindWriter(request.isBlindWriter())
                .build());

        // 본인 게시글의 본인 댓글에는 알림이 가지 않음
        if (post.getMember().getId().equals(writerId)) return;

        try {
            String pushNotificationContent = "\"" + post.getTitle() + "\"" + " 글에 댓글이 달렸어요.";

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
        reportCommentRepository.save(ReportComment.builder()
                .reporterId(memberId)
                .commentId(commentId)
                .createdAt(LocalDateTime.now(KST))
                .build());
    }
}
