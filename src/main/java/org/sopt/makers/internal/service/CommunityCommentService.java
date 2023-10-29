package org.sopt.makers.internal.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.domain.CommunityComment;
import org.sopt.makers.internal.dto.community.CommentDao;
import org.sopt.makers.internal.dto.community.CommentListResponse;
import org.sopt.makers.internal.dto.community.CommentSaveRequest;
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.repository.CommunityCommentRepository;
import org.sopt.makers.internal.repository.CommunityQueryRepository;
import org.sopt.makers.internal.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class CommunityCommentService {
    private final MemberRepository memberRepository;
    private final CommunityCommentRepository communityCommentsRepository;
    private final CommunityQueryRepository communityQueryRepository;

    @Transactional
    public void createComment(Long writerId, Long postId, CommentSaveRequest request) {
        val member = memberRepository.findById(writerId)
                .orElseThrow(() -> new NotFoundDBEntityException("Member"));
        // TODO: 게시글 생성 기능이 추가되면 해당 게시글이 존재하는지 확인하는 로직 추가 예정

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
    }

    @Transactional(readOnly = true)
    public List<CommentDao> getCommentLists(Long postId) {
        return communityQueryRepository.findCommentByPostId(postId);
    }

    @Transactional(readOnly = true)
    public List<CommentListResponse> getCommentList(Long postId) {
        return communityCommentsRepository.findAllByPostId(postId).stream()
                .map(comment -> CommentListResponse.builder()
                        .content(comment.getContent())
                        .parentCommentId(comment.getParentCommentId())
                        .isBlindWriter(comment.getIsBlindWriter())
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
