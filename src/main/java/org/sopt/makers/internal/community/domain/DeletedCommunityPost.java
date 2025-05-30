package org.sopt.makers.internal.community.domain;

import lombok.*;
import org.hibernate.annotations.Type;
import org.sopt.makers.internal.member.domain.Member;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table
public class DeletedCommunityPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @ManyToOne
    @JoinColumn(name = "writer_id")
    private Member member;

    @Column
    private Long categoryId;

    @Column
    private String title;

    @Column(length = 10000)
    private String content;

    @Builder.Default
    @Column
    private Integer hits = 0;

    @Type(type = "string-array")
    @Column(name = "images", columnDefinition = "text[]")
    private String[] images;

    @Builder.Default
    @Column
    private Boolean isQuestion = false;

    @Builder.Default
    @Column
    private Boolean isBlindWriter = false;

    @Builder.Default
    @Column
    private Boolean isReported = false;

    @Builder.Default
    @Column
    private LocalDateTime deletedAt = LocalDateTime.now();
}