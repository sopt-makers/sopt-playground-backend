package org.sopt.makers.internal.community.domain.comment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sopt.makers.internal.exception.ClientBadRequestException;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CommunityComment 테스트")
public class CommunityCommentTest {

    @Test
    @DisplayName("작성자가 본인의 댓글을 수정하면 검증을 통과한다")
    void validateUpdatePermission_Success() {
        // Given
        Long writerId = 1L;
        CommunityComment comment = CommunityComment.builder()
                .id(1L)
                .writerId(writerId)
                .content("원래 내용")
                .isBlindWriter(false)
                .isDeleted(false)
                .isReported(false)
                .build();

        // When & Then
        assertThatCode(() -> comment.validateUpdatePermission(writerId))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("다른 사용자가 댓글을 수정하면 예외가 발생한다")
    void validateUpdatePermission_Fail_NoPermission() {
        // Given
        Long writerId = 1L;
        Long otherUserId = 2L;
        CommunityComment comment = CommunityComment.builder()
                .id(1L)
                .writerId(writerId)
                .content("내용")
                .build();

        // When & Then
        assertThatThrownBy(() -> comment.validateUpdatePermission(otherUserId))
                .isInstanceOf(ClientBadRequestException.class);
    }

    @Test
    @DisplayName("삭제된 댓글을 수정하면 예외가 발생한다")
    void validateUpdatePermission_Fail_DeletedComment() {
        // Given
        Long writerId = 1L;
        CommunityComment comment = CommunityComment.builder()
                .id(1L)
                .writerId(writerId)
                .content("내용")
                .isDeleted(true)
                .build();

        // When & Then
        assertThatThrownBy(() -> comment.validateUpdatePermission(writerId))
                .isInstanceOf(ClientBadRequestException.class);
    }

    @Test
    @DisplayName("댓글 내용을 수정한다")
    void updateContent_Success() {
        // Given
        CommunityComment comment = CommunityComment.builder()
                .id(1L)
                .writerId(1L)
                .content("원래 내용")
                .build();

        // When
        comment.updateContent("수정된 내용");

        // Then
        assertThat(comment.getContent()).isEqualTo("수정된 내용");
    }

    @Test
    @DisplayName("익명 여부를 수정한다")
    void updateIsBlindWriter_Success() {
        // Given
        CommunityComment comment = CommunityComment.builder()
                .id(1L)
                .writerId(1L)
                .content("내용")
                .isBlindWriter(false)
                .build();

        // When
        comment.updateIsBlindWriter(true);

        // Then
        assertThat(comment.getIsBlindWriter()).isTrue();
    }
}
