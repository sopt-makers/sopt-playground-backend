package org.sopt.makers.internal.service.community.anonymous;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousNickname;
import org.sopt.makers.internal.community.repository.anonymous.AnonymousNicknameRepository;
import org.sopt.makers.internal.community.service.anonymous.AnonymousNicknameRetriever;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnonymousNicknameRetrieverTest {

    @InjectMocks
    private AnonymousNicknameRetriever anonymousNicknameRetriever;

    @Mock
    private AnonymousNicknameRepository anonymousNicknameRepository;


    @Test
    @DisplayName("recentUsedAnonymousNicknames가 비어있을 때 랜덤 닉네임을 반환한다.")
    void findRandomAnonymousNickname_whenRecentUsedIsEmpty() {
        // Given
        AnonymousNickname randomNickname = AnonymousNickname.builder().id(1L).nickname("Nickname1").build();
        when(anonymousNicknameRepository.findRandomOne()).thenReturn(randomNickname);

        // When
        AnonymousNickname result = anonymousNicknameRetriever.findRandomAnonymousNickname(List.of());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNickname()).isEqualTo("Nickname1");
        verify(anonymousNicknameRepository, times(1)).findRandomOne();
        verify(anonymousNicknameRepository, never()).findRandomOneByIdNotIn(any());
    }

    @Test
    @DisplayName("recentUsedAnonymousNicknames가 비어있지 않을 때 ID가 포함되지 않은 랜덤 닉네임을 반환한다.")
    void findRandomAnonymousNickname_whenRecentUsedIsNotEmpty() {
        // Given
        List<AnonymousNickname> recentUsedNicknames = List.of(
                AnonymousNickname.builder().id(1L).nickname("Nickname1").build(),
                AnonymousNickname.builder().id(2L).nickname("Nickname2").build()
        );
        AnonymousNickname randomNickname = AnonymousNickname.builder().id(3L).nickname("Nickname3").build();
        List<Long> excludedIds = recentUsedNicknames.stream().map(AnonymousNickname::getId).toList();

        when(anonymousNicknameRepository.findRandomOneByIdNotIn(excludedIds)).thenReturn(randomNickname);

        // When
        AnonymousNickname result = anonymousNicknameRetriever.findRandomAnonymousNickname(recentUsedNicknames);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getNickname()).isEqualTo("Nickname3");
        verify(anonymousNicknameRepository, never()).findRandomOne();
        verify(anonymousNicknameRepository, times(1)).findRandomOneByIdNotIn(excludedIds);
    }
}
