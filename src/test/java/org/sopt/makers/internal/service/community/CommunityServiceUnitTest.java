package org.sopt.makers.internal.service.community;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.internal.community.controller.dto.request.PostSaveRequest;
import org.sopt.makers.internal.community.domain.CommunityPost;
import org.sopt.makers.internal.community.service.CommunityPostModifier;
import org.sopt.makers.internal.community.service.CommunityPostService;
import org.sopt.makers.internal.community.service.anonymous.AnonymousPostProfileService;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.dto.community.PostSaveResponse;
import org.sopt.makers.internal.mapper.CommunityResponseMapper;
import org.sopt.makers.internal.member.service.MemberRetriever;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommunityServiceUnitTest {

    @InjectMocks
    private CommunityPostService communityPostService;

    @Mock
    private MemberRetriever memberRetriever;

    @Mock
    private CommunityPostModifier communityPostModifier;

    @Mock
    private AnonymousPostProfileService anonymousPostProfileService;

    @Mock
    private CommunityResponseMapper communityResponseMapper;

    @Test
    @DisplayName("커뮤니티 게시글 생성")
    public void testCreateCommunityPostWithBlindWriter() {
        // Given
        Long writerId = 1L;
        PostSaveRequest request = new PostSaveRequest(
                1L,
                "Sample Title",
                "Sample Content",
                true,
                true,
                new String[]{"image1.png", "image2.png"},
                "Sample Url"
        );

        Member mockMember = Member.builder()
                .id(writerId)
                .build();

        CommunityPost mockPost = CommunityPost.builder()
                .id(1L)
                .member(mockMember)
                .categoryId(1L)
                .title("Sample Title")
                .content("Sample Content")
                .images(new String[]{"image1.png", "image2.png"})
                .isQuestion(true)
                .isBlindWriter(true)
                .build();

        PostSaveResponse mockResponse = new PostSaveResponse(
                1L,
                1L,
                "Sample Title",
                "Sample Content",
                0,
                new String[]{"image1.jpg", "image2.jpg"},
                true,
                true,
                LocalDateTime.now()
        );

        when(memberRetriever.findMemberById(writerId)).thenReturn(mockMember);
        when(communityPostModifier.createCommunityPost(mockMember, request)).thenReturn(mockPost);
        doNothing().when(anonymousPostProfileService).createAnonymousPostProfile(mockMember, mockPost);
        when(communityResponseMapper.toPostSaveResponse(mockPost)).thenReturn(mockResponse);

        // When
        PostSaveResponse response = communityPostService.createPost(writerId, request);

        // Then
        verify(memberRetriever).findMemberById(writerId);
        verify(communityPostModifier).createCommunityPost(mockMember, request);
        verify(anonymousPostProfileService).createAnonymousPostProfile(mockMember, mockPost);
        verify(communityResponseMapper).toPostSaveResponse(mockPost);

        assertNotNull(response);
        assertEquals(mockResponse, response);
    }
}
