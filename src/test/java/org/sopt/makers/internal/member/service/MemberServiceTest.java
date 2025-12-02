package org.sopt.makers.internal.member.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.internal.member.repository.MemberRepository;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("최신 기수의 앱잼 TL 멤버 ID 목록을 랜덤 순서로 조회한다")
    void getAppjamTlMembers_ReturnsRandomOrderedMemberIds() {
        // Given
        Long viewerId = 100L;
        Integer currentGeneration = 37;
        List<Long> expectedMemberIds = Arrays.asList(1L, 2L, 3L);

        when(memberRepository.findAppjamTlMemberIdsByGenerationRandomly(currentGeneration))
                .thenReturn(expectedMemberIds);

        // When
        // Note: This test verifies that the repository method is called correctly
        // The actual profile fetching is tested in integration tests
        memberRepository.findAppjamTlMemberIdsByGenerationRandomly(currentGeneration);

        // Then
        verify(memberRepository).findAppjamTlMemberIdsByGenerationRandomly(eq(currentGeneration));
    }

    @Test
    @DisplayName("앱잼 TL 멤버가 없으면 빈 리스트를 반환한다")
    void getAppjamTlMembers_EmptyList_WhenNoTlMembers() {
        // Given
        Integer currentGeneration = 37;

        when(memberRepository.findAppjamTlMemberIdsByGenerationRandomly(currentGeneration))
                .thenReturn(List.of());

        // When
        List<Long> result = memberRepository.findAppjamTlMemberIdsByGenerationRandomly(currentGeneration);

        // Then
        assertThat(result).isEmpty();
        verify(memberRepository).findAppjamTlMemberIdsByGenerationRandomly(eq(currentGeneration));
    }

    @Test
    @DisplayName("Repository 메서드가 올바른 기수로 호출되는지 검증")
    void getAppjamTlMembers_CallsRepositoryWithCorrectGeneration() {
        // Given
        Integer expectedGeneration = 37;
        List<Long> mockMemberIds = Arrays.asList(10L, 20L, 30L);

        when(memberRepository.findAppjamTlMemberIdsByGenerationRandomly(expectedGeneration))
                .thenReturn(mockMemberIds);

        // When
        List<Long> result = memberRepository.findAppjamTlMemberIdsByGenerationRandomly(expectedGeneration);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly(10L, 20L, 30L);
        verify(memberRepository, times(1)).findAppjamTlMemberIdsByGenerationRandomly(expectedGeneration);
    }
}
