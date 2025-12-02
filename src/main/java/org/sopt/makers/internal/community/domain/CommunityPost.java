package org.sopt.makers.internal.community.domain;

import lombok.*;
import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import org.hibernate.annotations.Type;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousProfile;
import org.sopt.makers.internal.community.domain.comment.CommunityComment;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.common.AuditingTimeEntity;
import org.sopt.makers.internal.vote.domain.Vote;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table
public class CommunityPost extends AuditingTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private Long categoryId;

    @Column
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Builder.Default
    @Column(nullable = false)
    private Integer hits = 0;

    @Type(ListArrayType.class)
    @Column(name = "images", columnDefinition = "text[]")
    private List<String> images;

    @Column(nullable = false)
    private Boolean isQuestion;

    @Column(nullable = false)
    private Boolean isBlindWriter;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isReported = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isHot = false;

    private String sopticleUrl;

    @Builder.Default
    @OneToMany(cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JoinColumn(name = "postId")
    private List<CommunityComment> comments = new ArrayList<>();

    @OneToOne(mappedBy = "post", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Vote vote;

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<CommunityPostLike> likes = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anonymous_profile_id")
    private AnonymousProfile anonymousProfile;

    public void updatePost(Long categoryId, String title, String content,
                           List<String> images, Boolean isQuestion, Boolean isBlindWriter, String sopticleUrl) {
        this.categoryId = categoryId;
        this.title = title;
        this.content = content;
        this.images = images;
        this.isQuestion = isQuestion;
        this.isBlindWriter = isBlindWriter;
        this.sopticleUrl = sopticleUrl;
    }

    public void registerAnonymousProfile(AnonymousProfile profile) {
        this.anonymousProfile = profile;
    }
}