package org.sopt.makers.internal.community.domain;

import lombok.*;
import org.hibernate.annotations.Type;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.common.AuditingTimeEntity;
import org.sopt.makers.internal.domain.community.CommunityComment;

import javax.persistence.*;
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
    @JoinColumn(name = "writer_id")
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

    @Type(type = "string-array")
    @Column(name = "images", columnDefinition = "text[]")
    private String[] images;

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

    @Builder.Default
    @OneToMany(
            cascade = CascadeType.REMOVE,
            orphanRemoval = true
    )
    @JoinColumn(name = "postId")
    private List<CommunityComment> comments = new ArrayList<>();

    public void incrementHits() {
        this.hits++;
    }
}