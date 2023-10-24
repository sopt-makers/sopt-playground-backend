package org.sopt.makers.internal.domain.community;

import lombok.*;
import org.hibernate.annotations.Type;
import org.sopt.makers.internal.domain.CommunityComments;

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
    private Long id;

    @Column
    private String writerId;

    @Column
    private String title;

    @Column
    private String content;

    @Column
    private Integer hits;

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

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;


    @Builder.Default
    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JoinColumn(name = "postId")
    private List<CommunityComments> comments = new ArrayList<>();
}