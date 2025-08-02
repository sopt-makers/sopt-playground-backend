package org.sopt.makers.internal.community.mapper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import lombok.val;
import org.sopt.makers.internal.common.util.MentionCleaner;
import org.sopt.makers.internal.community.domain.CommunityPost;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousCommentProfile;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousPostProfile;
import org.sopt.makers.internal.community.domain.category.Category;
import org.sopt.makers.internal.community.dto.AnonymousProfileVo;
import org.sopt.makers.internal.community.dto.CategoryVo;
import org.sopt.makers.internal.community.dto.CommentInfo;
import org.sopt.makers.internal.community.dto.CommunityPostMemberVo;
import org.sopt.makers.internal.community.dto.CommunityPostVo;
import org.sopt.makers.internal.community.dto.MemberVo;
import org.sopt.makers.internal.community.dto.PostDetailData;
import org.sopt.makers.internal.community.dto.SoptActivityVo;
import org.sopt.makers.internal.community.dto.response.CommentResponse;
import org.sopt.makers.internal.community.dto.response.PopularPostResponse;
import org.sopt.makers.internal.community.dto.response.PostDetailResponse;
import org.sopt.makers.internal.community.dto.response.PostResponse;
import org.sopt.makers.internal.community.dto.response.PostSaveResponse;
import org.sopt.makers.internal.community.dto.response.PostUpdateResponse;
import org.sopt.makers.internal.community.dto.response.RecentPostResponse;
import org.sopt.makers.internal.community.dto.response.SopticlePostResponse;
import org.sopt.makers.internal.external.makers.CrewPost;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.SoptActivity;
import org.sopt.makers.internal.internal.dto.InternalPopularPostResponse;
import org.sopt.makers.internal.member.dto.response.MemberNameAndProfileImageResponse;
import org.sopt.makers.internal.vote.dto.response.VoteResponse;
import org.springframework.stereotype.Component;

import static java.util.stream.Collectors.*;

@Component
public class CommunityResponseMapper {
    public CommentResponse toCommentResponse(CommentInfo info, Long memberId) {
        val comment = info.commentDao().comment();
        val memberVo = comment.getIsBlindWriter() ? null : info.memberVo();
        val anonymousProfileVo = comment.getIsBlindWriter() && info.anonymousCommentProfile() != null
                ? toAnonymousCommentProfileVo(info.anonymousCommentProfile()) : null;

        val isMine = Objects.equals(info.commentDao().member().getId(), memberId);

        return new CommentResponse(comment.getId(), memberVo, isMine, comment.getPostId(), comment.getParentCommentId(),
                comment.getContent(), comment.getIsBlindWriter(), anonymousProfileVo, comment.getIsReported(), comment.getCreatedAt());
    }

//    public CommunityPostMemberVo toCommunityVo(CategoryPostMemberDao dao, VoteResponse voteResponse) {
//        val member = MemberVo.of(dao.member());
//        val category = toCategoryResponse(dao.category());
//        CommunityPostVo post = toPostVo(dao.post(), voteResponse);
//        return new CommunityPostMemberVo(member, post, category);
//    }

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

    public PostDetailResponse toPostDetailReponse(PostDetailData dto, Long viewerId, Boolean isLiked, Integer likes, AnonymousPostProfile anonymousPostProfile) {
        val postEntity = dto.post();
        val authorDetails = dto.userDetails();
        val memberVo = postEntity.getIsBlindWriter()
                ? null
                : MemberVo.of(authorDetails, dto.authorCareer());

        val isMine = Objects.equals(authorDetails.userId(), viewerId);

        val anonymousProfileVo = postEntity.getIsBlindWriter() && anonymousPostProfile != null
                ? toAnonymousPostProfileVo(anonymousPostProfile)
                : null;

        val postVo = toPostVo(postEntity, dto.vote());
        val categoryVo = toCategoryResponse(dto.category());

        return new PostDetailResponse(memberVo, postVo, categoryVo, isMine, isLiked, likes, anonymousProfileVo);
    }

    public CategoryVo toCategoryResponse(Category category) {
        if(category == null) return null;
        val parentCategoryName = category.getParent() == null ? null : category.getParent().getName();
        val parentId = category.getParent() == null ? null : category.getParent().getId();
        return new CategoryVo(category.getId(), category.getName(), parentId, parentCategoryName);
    }

    public CommunityPostVo toPostVo(CommunityPost post, VoteResponse voteResponse) {
        return new CommunityPostVo(post.getId(), post.getCategoryId(), post.getTitle(), post.getContent(), post.getHits(),
                post.getImages(), post.getIsQuestion(), post.getIsBlindWriter(), post.getSopticleUrl(), post.getIsReported(), post.getCreatedAt(), post.getUpdatedAt(),
                voteResponse);
    }

//    public CommentResponse toCommentResponse(CommentDao dao, Long memberId, AnonymousCommentProfile profile) {
//        val member = dao.comment().getIsBlindWriter() ? null : MemberVo.of(dao.member());
//        val comment = dao.comment();
//        val anonymousProfile = dao.comment().getIsBlindWriter() && profile != null? toAnonymousCommentProfileVo(profile) : null;
//        val isMine = Objects.equals(dao.member().getId(), memberId);
//        return new CommentResponse(comment.getId(), member, isMine, comment.getPostId(), comment.getParentCommentId(),
//                comment.getContent(), comment.getIsBlindWriter(), anonymousProfile, comment.getIsReported(), comment.getCreatedAt());
//    }

    public PostResponse toPostResponse (CommunityPostMemberVo dao, List<CommentInfo> commentInfos, Long memberId, AnonymousPostProfile anonymousPostProfile, Boolean isLiked, Integer likes) {
        val post = dao.post();
        val category = dao.category();
        val member = dao.post().isBlindWriter() ? null : dao.member();
        val writerId = dao.post().isBlindWriter() ? null : dao.member().id();
        val isMine = Objects.equals(dao.member().id(), memberId);
        val comments = commentInfos.stream()
                .map(info -> toCommentResponse(info, memberId))
                .collect(toList());
        val anonymousProfile = dao.post().isBlindWriter() && anonymousPostProfile != null ? toAnonymousPostProfileVo(anonymousPostProfile) : null;
        val createdAt = getRelativeTime(dao.post().createdAt());

        return new PostResponse(
                post.id(), member, writerId, isMine, isLiked, likes, post.categoryId(),
                category.name(), post.title(), post.content(), post.hits(),
                comments.size(), post.images(), post.isQuestion(), post.isBlindWriter(),
                post.sopticleUrl(), anonymousProfile, createdAt, comments, dao.post().vote()
        );
    }

    public PostResponse toPostResponse(CrewPost crewPost, Long viewerId) {
        val crewUser = crewPost.user();

        val soptActivityVo = new SoptActivityVo(
                crewUser.partInfo().generation(),
                crewUser.partInfo().part(),
                null
        );

        val memberVo = new MemberVo(
                crewUser.id(),
                crewUser.name(),
                crewUser.profileImage(),
                soptActivityVo,
                null
        );

        return new PostResponse(
                crewPost.id(),
                memberVo,
                crewUser.id(),
                Objects.equals(crewUser.orgId(), viewerId), // isMine
                crewPost.isLiked(),
                crewPost.likeCount(),
                24L, // 모임 카테고리 ID
                "모임", // 모임 카테고리 이름
                crewPost.title(),
                crewPost.contents(),
                crewPost.viewCount(),
                crewPost.commentCount(),
                crewPost.images(),
                false, // isQuestion
                false, // isBlindWriter
                null,  // sopticleUrl
                null,  // anonymousProfile
                getRelativeTime(crewPost.createdDate()),
                Collections.emptyList(), // comments
                null // vote
        );
    }

    public SopticlePostResponse toSopticlePostResponse(CommunityPost post, MemberVo memberVo) {
        return new SopticlePostResponse(
                post.getId(),
                memberVo,
                getRelativeTime(post.getCreatedAt()),
                post.getTitle(),
                post.getContent(),
                post.getImages(),
                post.getSopticleUrl()
        );
    }

//    public SopticlePostResponse toSopticlePostResponse(CommunityPost post) {
//        return new SopticlePostResponse(
//                post.getId(),
//                MemberVo.of(post.getMember()),
//                getRelativeTime(post.getCreatedAt()),
//                post.getTitle(),
//                post.getContent(),
//                post.getImages(),
//                post.getSopticleUrl()
//        );
//    }

    public RecentPostResponse toRecentPostResponse(CommunityPost post, int likeCount, int commentCount, Long categoryId, String categoryName, Integer totalVoteCount) {
        return new RecentPostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                getRelativeTime(post.getCreatedAt()),
                likeCount,
                commentCount,
                categoryId,
                categoryName,
                totalVoteCount,
                commentCount > 0
        );
    }

    private AnonymousProfileVo toAnonymousPostProfileVo(AnonymousPostProfile anonymousPostProfile) {
        return new AnonymousProfileVo(anonymousPostProfile.getNickname().getNickname(), anonymousPostProfile.getProfileImg().getImageUrl());
    }

    public AnonymousProfileVo toAnonymousCommentProfileVo(AnonymousCommentProfile profile) {
        return new AnonymousProfileVo(profile.getNickname().getNickname(), profile.getProfileImg().getImageUrl());
    }

//    public InternalCommunityPost toInternalCommunityPostResponse(PostCategoryDao dao) {
//        return new InternalCommunityPost(dao.post().getId(), dao.post().getTitle(), dao.category().getName(), dao.post().getImages(), dao.post().getIsHot(), dao.post().getContent());
//    }

    public InternalPopularPostResponse toInternalPopularPostResponse(CommunityPost post, AnonymousPostProfile anonymousPostProfile, InternalUserDetails userDetails, String categoryName, int rank) {
        if (Boolean.TRUE.equals(post.getIsBlindWriter()) && anonymousPostProfile != null) {
            // 익명일 경우
            return InternalPopularPostResponse.builder()
                    .id(post.getId())
                    .userId(null)
                    .profileImage(anonymousPostProfile.getProfileImg().getImageUrl())
                    .name(anonymousPostProfile.getNickname().getNickname())
                    .generationAndPart("")
                    .rank(rank)
                    .category(categoryName)
                    .title(post.getTitle())
                    .content(MentionCleaner.removeMentionIds(post.getContent()))
                    .webLink("https://playground.sopt.org/?feed=" + post.getId())
                    .build();
        } else {
            SoptActivity lastActivity = userDetails.soptActivities().stream()
                    .max(Comparator.comparing(SoptActivity::generation))
                    .orElse(null);
            String generationAndPart = String.format("%d기 %s", Objects.requireNonNull(lastActivity).generation(), lastActivity.part());
            return InternalPopularPostResponse.builder()
                    .id(post.getId())
                    .userId(userDetails.userId())
                    .profileImage(userDetails.profileImage())
                    .name(userDetails.name())
                    .generationAndPart(generationAndPart)
                    .rank(rank)
                    .category(categoryName)
                    .title(post.getTitle())
                    .content(MentionCleaner.removeMentionIds(post.getContent()))
                    .webLink("https://playground.sopt.org/?feed=" + post.getId())
                    .build();
        }
    }

    public PopularPostResponse toPopularPostResponse(CommunityPost post, AnonymousPostProfile anonymousPostProfile, InternalUserDetails userDetails, String categoryName) {
        MemberNameAndProfileImageResponse memberResponse;
        if (Boolean.TRUE.equals(post.getIsBlindWriter()) && anonymousPostProfile != null) {
            memberResponse = new MemberNameAndProfileImageResponse(
                    anonymousPostProfile.getId(),
                    anonymousPostProfile.getNickname().getNickname(),
                    anonymousPostProfile.getProfileImg().getImageUrl()
            );
        } else {
            memberResponse = MemberNameAndProfileImageResponse.from(userDetails);
        }

        return new PopularPostResponse(
                post.getId(), categoryName, post.getTitle(), memberResponse, post.getHits()
        );
    }

//    public PopularPostResponse toPopularPostResponse(CommunityPost post, AnonymousPostProfile anonymousPostProfile, String categoryName) {
//        MemberNameAndProfileImageResponse memberResponse;
//
//        if (Boolean.TRUE.equals(post.getIsBlindWriter()) && anonymousPostProfile != null) {
//            memberResponse = new MemberNameAndProfileImageResponse(
//                    anonymousPostProfile.getId(),
//                    anonymousPostProfile.getNickname().getNickname(),
//                    anonymousPostProfile.getProfileImg().getImageUrl()
//            );
//        } else {
//            memberResponse = MemberNameAndProfileImageResponse.from(post.getMember());
//        }
//
//        return new PopularPostResponse(
//                post.getId(),
//                categoryName,
//                post.getTitle(),
//                memberResponse,
//                post.getHits()
//        );
//    }

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
