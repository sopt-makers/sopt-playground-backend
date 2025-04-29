package org.sopt.makers.internal.mapper;

import lombok.val;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.MemberCareer;
import org.sopt.makers.internal.domain.community.AnonymousCommentProfile;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousPostProfile;
import org.sopt.makers.internal.community.domain.category.Category;
import org.sopt.makers.internal.community.domain.CommunityPost;
import org.sopt.makers.internal.dto.community.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class CommunityResponseMapper {
    public CommentResponse toCommentResponse(CommentDao dao, Long memberId, AnonymousCommentProfile profile) {
        val member = dao.comment().getIsBlindWriter() ? null : toMemberResponse(dao.member());
        val comment = dao.comment();
        val anonymousProfile = dao.comment().getIsBlindWriter() && profile != null? toAnonymousCommentProfileVo(profile) : null;
        val isMine = Objects.equals(dao.member().getId(), memberId);
        return new CommentResponse(comment.getId(), member, isMine, comment.getPostId(), comment.getParentCommentId(),
                comment.getContent(), comment.getIsBlindWriter(), anonymousProfile, comment.getIsReported(), comment.getCreatedAt());
    }

    public CommunityPostMemberVo toCommunityVo(CategoryPostMemberDao dao) {
        val member = toMemberResponse(dao.member());
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

    public MemberVo toMemberResponse(Member member) {
        if(member == null) return null;
        val career = (member.getCareers() == null || member.getCareers().stream().noneMatch(MemberCareer::getIsCurrent)) ?
                null : member.getCareers().stream().filter(MemberCareer::getIsCurrent).toList().get(0);
        member.getActivities().sort((act1, act2) -> (act2.getGeneration() - act1.getGeneration()));
        return new MemberVo(member.getId(),member.getName(), member.getProfileImage(),
                member.getActivities().get(0), career);
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

    private AnonymousProfileVo toAnonymousPostProfileVo(AnonymousPostProfile anonymousPostProfile) {
        return new AnonymousProfileVo(anonymousPostProfile.getNickname().getNickname(), anonymousPostProfile.getProfileImg().getImageUrl());
    }

    public AnonymousProfileVo toAnonymousCommentProfileVo(AnonymousCommentProfile profile) {
        return new AnonymousProfileVo(profile.getNickname().getNickname(), profile.getProfileImg().getImageUrl());
    }

    public InternalCommunityPost toInternalCommunityPostResponse(PostCategoryDao dao) {
        return new InternalCommunityPost(dao.post().getId(), dao.post().getTitle(), dao.category().getName(), dao.post().getImages(), dao.post().getIsHot(), dao.post().getContent());
    }
}
