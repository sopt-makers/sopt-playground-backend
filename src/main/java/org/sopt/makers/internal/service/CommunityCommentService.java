package org.sopt.makers.internal.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.domain.community.CommunityComment;
import org.sopt.makers.internal.dto.community.CommentDao;
import org.sopt.makers.internal.dto.community.CommentListResponse;
import org.sopt.makers.internal.dto.community.CommentSaveRequest;
import org.sopt.makers.internal.dto.pushNotification.PushNotificationRequest;
import org.sopt.makers.internal.dto.pushNotification.PushNotificationResponse;
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.repository.community.CommunityCommentRepository;
import org.sopt.makers.internal.repository.community.CommunityPostRepository;
import org.sopt.makers.internal.repository.community.CommunityQueryRepository;
import org.sopt.makers.internal.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class CommunityCommentService {
    private final MemberRepository memberRepository;
    private final CommunityCommentRepository communityCommentsRepository;
    private final CommunityPostRepository communityPostRepository;
    private final CommunityQueryRepository communityQueryRepository;
    private final InternalApiService internalApiService;
    private final PushNotificationService pushNotificationService;

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

        String pushNotificationTitle = "\"" + post.getTitle() + "\"" + " 글에 댓글이 달렸어요.";

        PushNotificationRequest pushNotificationRequest = PushNotificationRequest.builder()
                .title(pushNotificationTitle)
                .content("")
                .category("NEWS")
                .webLink(request.webLink())
                .userIds(new String[]{post.getMember().getId().toString()})
                .build();

        pushNotificationService.sendPushNotification(pushNotificationRequest);
    }

    @Transactional(readOnly = true)
    public List<CommentDao> getPostCommentList(Long postId) {
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
        communityCommentsRepository.delete(comment);
    }
}
