package org.sopt.makers.internal.community.service.comment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.internal.community.domain.comment.CommunityComment;
import org.sopt.makers.internal.community.dto.request.MentionRequest;
import org.sopt.makers.internal.community.dto.request.comment.CommentUpdateRequest;
import org.sopt.makers.internal.community.repository.comment.CommunityCommentRepository;
import org.sopt.makers.internal.community.service.anonymous.AnonymousProfileRetriever;
import org.sopt.makers.internal.community.service.anonymous.AnonymousProfileService;
import org.sopt.makers.internal.community.service.post.CommunityPostRetriever;
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.PlatformService;
import org.sopt.makers.internal.external.platform.SoptActivity;
import org.sopt.makers.internal.external.pushNotification.PushNotificationService;
import org.sopt.makers.internal.external.pushNotification.message.community.MentionNotificationMessage;
import org.sopt.makers.internal.member.service.MemberRetriever;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommunityCommentServiceTest {

    @Mock
    private CommunityCommentsRetriever communityCommentsRetriever;

    @Mock
    private CommunityCommentsModifier communityCommentsModifier;

    @Mock
    private CommunityPostRetriever communityPostRetriever;

    @Mock
    private MemberRetriever memberRetriever;

    @Mock
    private AnonymousProfileService anonymousProfileService;

    @Mock
    private AnonymousProfileRetriever anonymousProfileRetriever;

    @Mock
    private PushNotificationService pushNotificationService;

    @Mock
    private PlatformService platformService;

    @Mock
    private CommunityCommentRepository communityCommentRepository;

    @InjectMocks
    private CommunityCommentService communityCommentService;

    @Test
    @DisplayName("댓글 작성자가 본인의 댓글을 수정하면 성공한다")
    void updateComment_Success() {
        // Given
        Long writerId = 1L;
        Long commentId = 1L;
        Long postId = 1L;

        CommunityComment comment = CommunityComment.builder()
                .id(commentId)
                .writerId(writerId)
                .postId(postId)
                .content("원래 내용")
                .isBlindWriter(false)
                .isDeleted(false)
                .isReported(false)
                .build();

        CommentUpdateRequest request = new CommentUpdateRequest(
                "수정된 내용",
                false,
                "https://example.com",
                null,
                null
        );

        when(communityCommentsRetriever.findCommunityCommentById(commentId))
                .thenReturn(comment);

        // When
        communityCommentService.updateComment(writerId, commentId, request);

        // Then
        verify(communityCommentsModifier).updateCommunityComment(comment, request);
    }

    @Test
    @DisplayName("다른 사용자의 댓글을 수정하면 예외가 발생한다")
    void updateComment_Fail_NoPermission() {
        // Given
        Long writerId = 1L;
        Long otherUserId = 2L;
        Long commentId = 1L;

        CommunityComment comment = CommunityComment.builder()
                .id(commentId)
                .writerId(writerId)
                .content("내용")
                .build();

        CommentUpdateRequest request = new CommentUpdateRequest(
                "수정 시도",
                false,
                "https://example.com",
                null,
                null
        );

        when(communityCommentsRetriever.findCommunityCommentById(commentId))
                .thenReturn(comment);

        // When & Then
        assertThatThrownBy(() ->
                communityCommentService.updateComment(otherUserId, commentId, request)
        )
                .isInstanceOf(ClientBadRequestException.class);
    }

    @Test
    @DisplayName("댓글 수정 시 새로운 일반 멘션에게만 알림을 발송한다")
    void updateComment_SendNotificationToNewMentionsOnly() {
        // Given
        Long writerId = 1L;
        Long commentId = 1L;
        Long postId = 1L;
        String oldContent = "안녕 @홍길동[123]";
        Long[] newMentionIds = {123L, 456L};

        CommunityComment comment = CommunityComment.builder()
                .id(commentId)
                .writerId(writerId)
                .postId(postId)
                .content(oldContent)
                .isBlindWriter(false)
                .isDeleted(false)
                .build();

        CommentUpdateRequest request = new CommentUpdateRequest(
                "안녕 @홍길동[123] @철수[456]",
                false,
                "https://example.com",
                new MentionRequest(newMentionIds, "작성자", "https://example.com"),
                null
        );

        when(communityCommentsRetriever.findCommunityCommentById(commentId))
                .thenReturn(comment);
        when(platformService.getInternalUser(writerId))
                .thenReturn(new InternalUserDetails(writerId, "작성자", "profile.jpg", "", "", "", 1, List.of(new SoptActivity(1, 1, "", "", ""))));

        // When
        communityCommentService.updateComment(writerId, commentId, request);

        // Then
        ArgumentCaptor<MentionNotificationMessage> captor =
                ArgumentCaptor.forClass(MentionNotificationMessage.class);
        verify(pushNotificationService).sendPushNotification(captor.capture());

        MentionNotificationMessage message = captor.getValue();
        assertThat(message.getRecipientIds())
                .containsExactly(456L)
                .doesNotContain(123L);
    }

    @Test
    @DisplayName("모두 기존 멘션이면 알림을 발송하지 않는다")
    void updateComment_NoNotificationIfAllExistingMentions() {
        // Given
        Long writerId = 1L;
        Long commentId = 1L;
        Long postId = 1L;
        String oldContent = "안녕 @홍길동[123] @철수[456]";
        Long[] newMentionIds = {123L, 456L};

        CommunityComment comment = CommunityComment.builder()
                .id(commentId)
                .writerId(writerId)
                .postId(postId)
                .content(oldContent)
                .isBlindWriter(false)
                .build();

        CommentUpdateRequest request = new CommentUpdateRequest(
                "안녕 @홍길동[123] @철수[456]",
                false,
                "https://example.com",
                new MentionRequest(newMentionIds, "작성자", "https://example.com"),
                null
        );

        when(communityCommentsRetriever.findCommunityCommentById(commentId))
                .thenReturn(comment);

        // When
        communityCommentService.updateComment(writerId, commentId, request);

        // Then
        verify(pushNotificationService, never()).sendPushNotification(any());
    }
}
