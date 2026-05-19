package org.sopt.makers.internal.community.mapper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.val;

import org.hibernate.Hibernate;
import org.sopt.makers.internal.community.domain.enums.CommunityCategoryCode;
import org.sopt.makers.internal.community.domain.enums.CommunityCategoryGroup;
import org.sopt.makers.internal.community.domain.enums.CommunityPostSourceType;
import org.sopt.makers.internal.community.domain.enums.CommunityPostTag;
import org.sopt.makers.internal.community.utils.MentionCleaner;
import org.sopt.makers.internal.community.domain.CommunityPost;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousProfile;
import org.sopt.makers.internal.community.domain.category.Category;
import org.sopt.makers.internal.community.dto.AnonymousProfileVo;
import org.sopt.makers.internal.community.dto.CategoryVo;
import org.sopt.makers.internal.community.dto.comment.CommentInfo;
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
    public CommentResponse toCommentResponse(CommentInfo info, Long memberId, Boolean isLiked, Integer likeCount) {
        val comment = info.commentDao().comment();
        if (Boolean.TRUE.equals(comment.getIsDeleted())) {
            return new CommentResponse(
                    comment.getId(),
                    null,
                    null,
                    comment.getPostId(),
                    comment.getParentCommentId(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    true,
                    null,
                    null,
                    new ArrayList<>()
            );
        }

        val memberVo = comment.getIsBlindWriter() ? null : info.memberVo();
        val anonymousProfileVo = comment.getIsBlindWriter() && info.anonymousProfile() != null
                ? toAnonymousProfileVo(info.anonymousProfile()) : null;
        val isMine = Objects.equals(info.commentDao().member().getId(), memberId);

        return new CommentResponse(
                comment.getId(),
                memberVo,
                isMine,
                comment.getPostId(),
                comment.getParentCommentId(),
                comment.getContent(),
                comment.getIsBlindWriter(),
                anonymousProfileVo,
                comment.getIsReported(),
                comment.getCreatedAt(),
                false,
                isLiked,
                likeCount,
                new ArrayList<>()
        );
    }

    public PostUpdateResponse toPostUpdateResponse(CommunityPost post) {
        return new PostUpdateResponse(post.getId(), post.getCategory().getCode(), post.getTitle(),
                post.getContent(), post.getHits(), post.getImages(),
                post.getIsBlindWriter(), post.getCreatedAt(), post.getUpdatedAt());
    }

    public PostSaveResponse toPostSaveResponse(CommunityPost post) {
        return new PostSaveResponse(post.getId(), post.getCategory().getCode(), post.getTitle(),
                post.getContent(), post.getHits(), post.getImages(),
                post.getIsBlindWriter(), post.getCreatedAt());
    }

    public PostDetailResponse toPostDetailReponse(
        PostDetailData dto,
        Long viewerId,
        Boolean isLiked,
        Integer likes,
        AnonymousProfile anonymousProfile
    ) {
        val postEntity = dto.post();
        val authorDetails = dto.userDetails();
        val memberVo = postEntity.getIsBlindWriter()
            ? null
            : MemberVo.of(authorDetails, dto.authorCareer());

        val isMine = Objects.equals(authorDetails.userId(), viewerId);

        val anonymousProfileVo = postEntity.getIsBlindWriter() && anonymousProfile != null
            ? toAnonymousProfileVo(anonymousProfile)
            : null;

        val postVo = toPostVo(postEntity, dto.category(), dto.vote());
        val categoryVo = toCategoryResponse(dto.category());

        return new PostDetailResponse(memberVo, postVo, categoryVo, isMine, isLiked, likes, anonymousProfileVo);
    }

    public CategoryVo toCategoryResponse(Category category) {
        if (category == null) {
            return null;
        }

        Category parent = category.getParent();

        CommunityCategoryCode parentCode = null;
        String parentCategoryName = null;

        if (parent != null && Hibernate.isInitialized(parent)) {
            parentCode = parent.getCode();
            parentCategoryName = parent.getName();
        }

        return new CategoryVo(
            category.getCategoryGroup(),
            category.getCode(),
            category.getName(),
            parentCode,
            parentCategoryName
        );
    }

    public CommunityPostVo toPostVo(
        CommunityPost post,
        Category category,
        VoteResponse voteResponse
    ) {
        Category resolvedCategory = category != null ? category : post.getCategory();
        return new CommunityPostVo(
            post.getId(),
            resolvedCategory == null ? null : resolvedCategory.getCategoryGroup(),
            resolvedCategory == null ? null : resolvedCategory.getCode(),
            post.getTitle(),
            post.getContent(),
            post.getHits(),
            post.getImages(),
            post.getIsBlindWriter(),
            post.getSopticleUrl(),
            post.getIsReported(),
            post.getCreatedAt(),
            post.getUpdatedAt(),
            voteResponse
        );
    }

    public CommunityPostVo toPostVo(CommunityPost post, VoteResponse voteResponse) {
        return toPostVo(post, post.getCategory(), voteResponse);
    }

    public PostResponse toPostResponse(
        CommunityPostMemberVo dao,
        List<CommentInfo> commentInfos,
        Long memberId,
        AnonymousProfile anonymousProfile,
        Boolean isLiked,
        Integer likes,
        Map<Long, Boolean> commentLikedMap,
        Map<Long, Integer> commentLikeCountMap
    ) {
        val post = dao.post();
        val category = dao.category();
        val member = dao.post().isBlindWriter() ? null : dao.member();
        val writerId = dao.post().isBlindWriter() ? null : dao.member().id();
        val isMine = Objects.equals(dao.member().id(), memberId);

        val comments = commentInfos.stream()
            .map(info -> {
                Long commentId = info.commentDao().comment().getId();
                Boolean commentIsLiked = commentLikedMap.getOrDefault(commentId, false);
                Integer commentLikeCount = commentLikeCountMap.getOrDefault(commentId, 0);
                return toCommentResponse(info, memberId, commentIsLiked, commentLikeCount);
            })
            .collect(toList());

        val anonymousProfileVo = dao.post().isBlindWriter() && anonymousProfile != null
            ? toAnonymousProfileVo(anonymousProfile)
            : null;

        val createdAt = getRelativeTime(dao.post().createdAt());

        return new PostResponse(
            post.id(),
            CommunityPostSourceType.COMMUNITY,
            member,
            writerId,
            isMine,
            isLiked,
            likes,
            category == null ? null : category.categoryGroup(),
            category == null ? null : category.code(),
            category == null ? null : category.name(),
            List.of(),
            post.title(),
            post.content(),
            post.hits(),
            (int) comments.stream().filter(comment -> !comment.isDeleted()).count(),
            post.images(),
            post.isBlindWriter(),
            post.sopticleUrl(),
            anonymousProfileVo,
            createdAt,
            comments,
            dao.post().vote(),
            null
        );
    }

    public PostResponse toPostResponse(CrewPost crewPost, Long viewerId) {
        val crewUser = crewPost.user();

        Long writerId = crewUser.orgId();

        val soptActivityVo = new SoptActivityVo(
            crewUser.partInfo().generation(),
            crewUser.partInfo().part(),
            null
        );

        val memberVo = new MemberVo(
            writerId,
            crewUser.name(),
            crewUser.profileImage(),
            soptActivityVo,
            null
        );

        return new PostResponse(
            crewPost.id(),
            CommunityPostSourceType.MEETING,
            memberVo,
            writerId,
            Objects.equals(writerId, viewerId),
            crewPost.isLiked(),
            crewPost.likeCount(),
            CommunityCategoryGroup.FREE,
            CommunityCategoryCode.FREE,
            "자유",
            List.of(CommunityPostTag.MEETING),
            crewPost.title(),
            crewPost.contents(),
            crewPost.viewCount(),
            crewPost.commentCount(),
            crewPost.images(),
            false,
            null,
            null,
            getRelativeTime(crewPost.createdDate()),
            Collections.emptyList(),
            null,
            crewPost.meetingId()
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

    public RecentPostResponse toRecentPostResponse(
        CommunityPost post,
        int likeCount,
        int commentCount,
        CommunityPostTag categoryTag,
        Integer totalVoteCount
    ) {
        return new RecentPostResponse(
            post.getId(),
            post.getTitle(),
            post.getContent(),
            getRelativeTime(post.getCreatedAt()),
            likeCount,
            commentCount,
            categoryTag,
            categoryTag == null ? null : categoryTag.getLabel(),
            totalVoteCount
        );
    }

    public RecentPostResponse toRecentPostResponse(CrewPost crewPost) {
        CommunityPostTag categoryTag = CommunityPostTag.MEETING;

        return new RecentPostResponse(
            crewPost.id(),
            crewPost.title(),
            crewPost.contents(),
            getRelativeTime(crewPost.createdDate()),
            crewPost.likeCount(),
            crewPost.commentCount(),
            categoryTag,
            categoryTag.getLabel(),
            null
        );
    }

    private AnonymousProfileVo toAnonymousProfileVo(AnonymousProfile anonymousProfile) {
        return new AnonymousProfileVo(anonymousProfile.getNickname().getNickname(), anonymousProfile.getProfileImg().getImageUrl());
    }

    public InternalPopularPostResponse toInternalPopularPostResponse(
        CommunityPost post,
        AnonymousProfile anonymousProfile,
        InternalUserDetails userDetails,
        String categoryName,
        int rank,
        String baseUrl
    ) {
        if (Boolean.TRUE.equals(post.getIsBlindWriter())) {
            if (anonymousProfile == null) {
                throw new IllegalStateException("anonymous profile is required for blind post");
            }

            return InternalPopularPostResponse.builder()
                .id(post.getId())
                .userId(null)
                .profileImage(anonymousProfile.getProfileImg().getImageUrl())
                .name(anonymousProfile.getNickname().getNickname())
                .generationAndPart("")
                .rank(rank)
                .category(categoryName)
                .title(post.getTitle())
                .content(MentionCleaner.removeMentionIds(post.getContent()))
                .webLink(baseUrl + post.getId())
                .build();
        }

        if (userDetails == null) {
            throw new IllegalStateException("user details is required for non-blind post");
        }

        SoptActivity lastActivity = userDetails.soptActivities() == null
            ? null
            : userDetails.soptActivities().stream()
              .max(Comparator.comparing(SoptActivity::generation))
              .orElse(null);

        String generationAndPart = lastActivity == null
            ? ""
            : String.format("%d기 %s", lastActivity.generation(), lastActivity.part());

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
            .webLink(baseUrl + post.getId())
            .build();
    }

    public PopularPostResponse toPopularPostResponse(
        CommunityPost post,
        AnonymousProfile anonymousProfile,
        InternalUserDetails userDetails,
        int likeCount,
        int commentCount,
        CommunityPostTag categoryTag
    ) {
        MemberNameAndProfileImageResponse memberResponse;

        if (Boolean.TRUE.equals(post.getIsBlindWriter())) {
            if (anonymousProfile == null) {
                throw new IllegalStateException("anonymous profile is required for blind post");
            }

            memberResponse = new MemberNameAndProfileImageResponse(
                anonymousProfile.getId(),
                anonymousProfile.getNickname().getNickname(),
                anonymousProfile.getProfileImg().getImageUrl()
            );
        } else {
            if (userDetails == null) {
                throw new IllegalStateException("user details is required for non-blind post");
            }
            memberResponse = MemberNameAndProfileImageResponse.from(userDetails);
        }

        return new PopularPostResponse(
            post.getId(),
            post.getTitle(),
            memberResponse,
            post.getHits(),
            likeCount,
            commentCount,
            categoryTag,
            categoryTag == null ? null : categoryTag.getLabel()
        );
    }

    public PopularPostResponse toPopularPostResponse(CrewPost crewPost) {
        CrewPost.CrewUser crewUser = crewPost.user();
        CommunityPostTag categoryTag = CommunityPostTag.MEETING;

        MemberNameAndProfileImageResponse memberResponse = new MemberNameAndProfileImageResponse(
            crewUser.orgId(),
            crewUser.name(),
            crewUser.profileImage()
        );

        return new PopularPostResponse(
            crewPost.id(),
            crewPost.title(),
            memberResponse,
            crewPost.viewCount(),       // hits 의미: 조회수
            crewPost.likeCount(),
            crewPost.commentCount(),
            categoryTag,
            categoryTag.getLabel()
        );
    }

    public List<CommentResponse> buildCommentHierarchy(List<CommentResponse> flatComments) {
        Map<Long, CommentResponse> commentMap = new HashMap<>();
        List<CommentResponse> topLevelComments = new ArrayList<>();

        for (CommentResponse comment : flatComments) {
            commentMap.put(comment.id(), comment);
        }

        for (CommentResponse comment : flatComments) {
            if (comment.parentCommentId() == null) {
                topLevelComments.add(comment);
            } else {
                CommentResponse parentComment = commentMap.get(comment.parentCommentId());
                if (parentComment != null) {
                    parentComment.replies().add(comment);
                }
            }
        }

        return topLevelComments;
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
