package org.sopt.makers.internal.community.domain.comment;

import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.sopt.makers.internal.common.AuditingTimeEntity;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousProfile;

import jakarta.persistence.*;
import org.sopt.makers.internal.exception.ClientBadRequestException;

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

    @Column
    private Long postId;

    @Column(nullable = false)
    private Long writerId;

    private Long parentCommentId;

    @Column(nullable = false)
    private Boolean isBlindWriter;

    @ColumnDefault("false")
    private Boolean isReported;

    @ColumnDefault("false")
    @Column(nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anonymous_profile_id")
    private AnonymousProfile anonymousProfile;

    public void registerAnonymousProfile(AnonymousProfile profile) {
        this.anonymousProfile = profile;
    }

    public void updateContent(String newContent) {
        this.content = newContent;
    }

    public void validateUpdatePermission(Long requestUserId) {
        if (!this.writerId.equals(requestUserId)) {
            throw new ClientBadRequestException("댓글 수정 권한이 없습니다.");
        }

        if (Boolean.TRUE.equals(this.isDeleted)) {
            throw new ClientBadRequestException("삭제된 댓글은 수정할 수 없습니다.");
        }
    }

    public void markAsDeleted() {
        this.isDeleted = true;
    }
}
