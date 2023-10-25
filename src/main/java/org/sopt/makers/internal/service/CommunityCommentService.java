package org.sopt.makers.internal.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.domain.CommunityComment;
import org.sopt.makers.internal.dto.community.CommentSaveRequest;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.repository.CommunityCommentRepository;
import org.sopt.makers.internal.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CommunityCommentService {
    private final MemberRepository memberRepository;
    private final CommunityCommentRepository communityCommentsRepository;
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
                        .writerId(writerId)
                        .parentCommentId(request.parentCommentId())
                        .isBlindWriter(request.isBlindWriter())
                .build());
    }
}
