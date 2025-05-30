package org.sopt.makers.internal.community.domain.comment;

import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@DynamicInsert
public class DeletedCommunityComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Column
    private Long postId;

    @Column(nullable = false)
    private Long writerId;

    private Long parentCommentId;

    @Column(nullable = false)
    private Boolean isBlindWriter;

    @ColumnDefault("false")
    private Boolean isReported;

    @Builder.Default
    @Column
    private LocalDateTime deletedAt = LocalDateTime.now();
}
