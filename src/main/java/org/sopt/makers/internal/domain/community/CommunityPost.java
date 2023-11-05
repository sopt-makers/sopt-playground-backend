package org.sopt.makers.internal.domain.community;

import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table
public class CommunityPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @Column
    private Long writerId;

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
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt;


    @Builder.Default
    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JoinColumn(name = "postId")
    private List<CommunityComment> comments = new ArrayList<>();
}