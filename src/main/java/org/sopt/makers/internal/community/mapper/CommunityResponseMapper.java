package org.sopt.makers.internal.community.mapper;

import java.time.Duration;
import java.time.LocalDateTime;
import lombok.val;
import org.sopt.makers.internal.community.dto.*;
import org.sopt.makers.internal.community.dto.response.*;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousCommentProfile;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousPostProfile;
import org.sopt.makers.internal.community.domain.category.Category;
import org.sopt.makers.internal.community.domain.CommunityPost;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class CommunityResponseMapper {
    public CommentResponse toCommentResponse(CommentDao dao, Long memberId, AnonymousCommentProfile profile) {
        val member = dao.comment().getIsBlindWriter() ? null : MemberVo.of(dao.member());
        val comment = dao.comment();
        val anonymousProfile = dao.comment().getIsBlindWriter() && profile != null? toAnonymousCommentProfileVo(profile) : null;
        val isMine = Objects.equals(dao.member().getId(), memberId);
        return new CommentResponse(comment.getId(), member, isMine, comment.getPostId(), comment.getParentCommentId(),
                comment.getContent(), comment.getIsBlindWriter(), anonymousProfile, comment.getIsReported(), comment.getCreatedAt());
    }

    public CommunityPostMemberVo toCommunityVo(CategoryPostMemberDao dao) {
        val member = MemberVo.of(dao.member());
        val category = toCategoryResponse(dao.category());
        val post = toPostVo(dao.posts());
        return new CommunityPostMemberVo(member, post, category);
    }

    public PostUpdateResponse toPostUpdateResponse(CommunityPost post) {
        return new PostUpdateResponse(post.getId(), post.getCategoryId(), post.getTitle(),
                post.getContent(), post.getHits(), post.getImages(), post.getIsQuestion(),
                post.getIsBlindWriter(), post.getCreatedAt(), post.getUpdatedAt());
    }

    public PostSaveResponse toPostSaveResponse(CommunityPost post) {
        return new PostSaveResponse(post.getId(), post.getCategoryId(), post.getTitle(),
                post.getContent(), post.getHits(), post.getImages(), post.getIsQuestion(),
                post.getIsBlindWriter(), post.getCreatedAt());
    }

    public PostDetailResponse toPostDetailReponse(CommunityPostMemberVo post, Long memberId, Boolean isLiked, Integer likes, AnonymousPostProfile anonymousPostProfile) {
        val member = post.post().isBlindWriter() ? null : post.member();
        val isMine = Objects.equals(post.member().id(), memberId);
        val anonymousProfile = post.post().isBlindWriter() && anonymousPostProfile != null ? toAnonymousPostProfileVo(anonymousPostProfile) : null;
        return new PostDetailResponse(member, post.post(), post.category(), isMine, isLiked, likes, anonymousProfile);
    }

    public CategoryVo toCategoryResponse(Category category) {
        if(category == null) return null;
        val parentCategoryName = category.getParent() == null ? null : category.getParent().getName();
        val parentId = category.getParent() == null ? null : category.getParent().getId();
        return new CategoryVo(category.getId(), category.getName(), parentId, parentCategoryName);
    }

    public CommunityPostVo toPostVo(CommunityPost post) {
        return new CommunityPostVo(post.getId(), post.getCategoryId(), post.getTitle(), post.getContent(), post.getHits(),
                post.getImages(), post.getIsQuestion(), post.getIsBlindWriter(), post.getSopticleUrl(), post.getIsReported(), post.getCreatedAt(), post.getUpdatedAt());
    }

    public PostResponse toPostResponse (CommunityPostMemberVo dao, List<CommentDao> commentDaos, Long memberId, AnonymousPostProfile anonymousPostProfile, Boolean isLiked, Integer likes) {
        val post = dao.post();
        val category = dao.category();
        val member = dao.post().isBlindWriter() ? null : dao.member();
        val writerId = dao.post().isBlindWriter() ? null : dao.member().id();
        val isMine = Objects.equals(dao.member().id(), memberId);
        val comments = commentDaos.stream().map(comment -> toCommentResponse(comment, memberId, null)).collect(Collectors.toList());
        val anonymousProfile = dao.post().isBlindWriter() && anonymousPostProfile != null ? toAnonymousPostProfileVo(anonymousPostProfile) : null;
        return new PostResponse(post.id(), member, writerId, isMine, isLiked, likes, post.categoryId(), category.name(), post.title(), post.content(), post.hits(),
                comments.size(), post.images(), post.isQuestion(), post.isBlindWriter(), post.sopticleUrl(), anonymousProfile, post.createdAt(), comments);
    }

    public SopticlePostResponse toSopticlePostResponse(CommunityPost post) {
        return new SopticlePostResponse(
                post.getId(),
                MemberVo.of(post.getMember()),
                getRelativeTime(post.getCreatedAt()),
                post.getTitle(),
                post.getContent(),
                post.getImages(),
                post.getSopticleUrl()
        );
    }

    public QuestionPostResponse toQuestionPostResponse(CommunityPost post, int likeCount, int commentCount) {
        return new QuestionPostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                getRelativeTime(post.getCreatedAt()),
                likeCount,
                commentCount,
                commentCount > 0
        );
    }

    private AnonymousProfileVo toAnonymousPostProfileVo(AnonymousPostProfile anonymousPostProfile) {
        return new AnonymousProfileVo(anonymousPostProfile.getNickname().getNickname(), anonymousPostProfile.getProfileImg().getImageUrl());
    }

    public AnonymousProfileVo toAnonymousCommentProfileVo(AnonymousCommentProfile profile) {
        return new AnonymousProfileVo(profile.getNickname().getNickname(), profile.getProfileImg().getImageUrl());
    }

    public InternalCommunityPost toInternalCommunityPostResponse(PostCategoryDao dao) {
        return new InternalCommunityPost(dao.post().getId(), dao.post().getTitle(), dao.category().getName(), dao.post().getImages(), dao.post().getIsHot(), dao.post().getContent());
    }

    private String getRelativeTime(LocalDateTime createdAt) {
        Duration duration = Duration.between(createdAt, LocalDateTime.now());

        long seconds = duration.getSeconds();
        if(seconds < 60) return "몇초 전";

        long minutes = seconds / 60;
        if(minutes < 60) return minutes + "분 전";

        long hours = minutes / 60;
        if(hours < 24) return hours + "시간 전";

        long days = hours / 24;
        if(days < 7) return days + "일 전";

        long weeks = days / 7;
        if(weeks < 5) return weeks + "주 전";

        long months = days / 30;
        if(months < 12) return months + "개월 전";

        long years = months / 12;
        return years + "년 전";
    }
}
