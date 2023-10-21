package org.sopt.makers.internal.domain;

import lombok.*;
import org.sopt.makers.internal.domain.common.AuditingTimeEntity;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class CommunityComments extends AuditingTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private Long postId;

    @Column(nullable = false)
    private Long writerId;

    @Column(nullable = false)
    private Long parentCommentId;

    @Column(nullable = false)
    private Boolean isBlindWriter;
}
