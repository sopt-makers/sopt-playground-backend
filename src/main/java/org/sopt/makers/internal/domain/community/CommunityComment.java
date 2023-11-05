package org.sopt.makers.internal.domain.community;

import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.sopt.makers.internal.domain.common.AuditingTimeEntity;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@DynamicInsert
public class CommunityComment extends AuditingTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private Long postId;

    @Column(nullable = false)
    private Long writerId;

    private Long parentCommentId;

    @Column(nullable = false)
    private Boolean isBlindWriter;

    @ColumnDefault("false")
    private Boolean isReported;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "post_id")
    private CommunityPost post;
}
