package org.sopt.makers.internal.community.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MentionExtractor 테스트")
public class MentionExtractorTest {

    @Nested
    @DisplayName("extractMentionedUserIds 메서드는")
    class ExtractMentionedUserIdsTest {

        @Test
        @DisplayName("content에서 멘션된 userId를 추출한다")
        void extractMentionedUserIds_Success() {
            // Given
            String content = "안녕하세요 @홍길동[123] 님, @철수[456] 님";

            // When
            Set<Long> result = MentionExtractor.extractMentionedUserIds(content);

            // Then
            assertThat(result).containsExactlyInAnyOrder(123L, 456L);
        }

        @Test
        @DisplayName("멘션이 없으면 빈 Set을 반환한다")
        void extractMentionedUserIds_NoMention() {
            // Given
            String content = "안녕하세요 멘션이 없습니다";

            // When
            Set<Long> result = MentionExtractor.extractMentionedUserIds(content);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("중복된 멘션은 하나만 추출한다")
        void extractMentionedUserIds_Duplicates() {
            // Given
            String content = "@홍길동[123] 님 @홍길동[123] 님 @철수[456] 님";

            // When
            Set<Long> result = MentionExtractor.extractMentionedUserIds(content);

            // Then
            assertThat(result).hasSize(2)
                    .containsExactlyInAnyOrder(123L, 456L);
        }

        @Test
        @DisplayName("여러 멘션이 연속으로 있어도 모두 추출한다")
        void extractMentionedUserIds_MultipleMentions() {
            // Given
            String content = "@사용자1[100] @사용자2[200] @사용자3[300]";

            // When
            Set<Long> result = MentionExtractor.extractMentionedUserIds(content);

            // Then
            assertThat(result).containsExactlyInAnyOrder(100L, 200L, 300L);
        }

        @Test
        @DisplayName("content가 빈 문자열이면 빈 Set을 반환한다")
        void extractMentionedUserIds_EmptyContent() {
            // Given
            String content = "";

            // When
            Set<Long> result = MentionExtractor.extractMentionedUserIds(content);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("content가 공백만 있으면 빈 Set을 반환한다")
        void extractMentionedUserIds_BlankContent() {
            // Given
            String content = " ";

            // When
            Set<Long> result = MentionExtractor.extractMentionedUserIds(content);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("잘못된 형식의 멘션은 무시한다")
        void extractMentionedUserIds_InvalidFormat() {
            // Given
            String content = "@잘못된형식 @올바른[123] @괄호없음[456";

            // When
            Set<Long> result = MentionExtractor.extractMentionedUserIds(content);

            // Then
            assertThat(result).containsExactly(123L);
        }

        @Test
        @DisplayName("멘션 사이에 다른 텍스트가 있어도 정상 추출한다")
        void extractMentionedUserIds_WithOtherText() {
            // Given
            String content = "안녕하세요 @홍길동[123] 님! 오늘 날씨가 좋네요. @철수[456] 님도 그렇게 생각하시나요?";

            // When
            Set<Long> result = MentionExtractor.extractMentionedUserIds(content);

            // Then
            assertThat(result).containsExactlyInAnyOrder(123L, 456L);
        }

        @Test
        @DisplayName("숫자가 아닌 ID는 무시한다")
        void extractMentionedUserIds_NonNumericId() {
            // Given
            String content = "@사용자[abc] @올바른[123]";

            // When
            Set<Long> result = MentionExtractor.extractMentionedUserIds(content);

            // Then
            assertThat(result).containsExactly(123L);
        }
    }

    @Nested
    @DisplayName("getNewlyAddedMentions 메서드는")
    class GetNewlyAddedMentionsTest {

        @Test
        @DisplayName("새롭게 추가된 멘션만 추출한다")
        void getNewlyAddedMentions_Success() {
            // Given
            String oldContent = "안녕 @홍길동[123] @철수[456]";
            Long[] newMentionIds = {123L, 789L, 999L};

            // When
            Long[] result = MentionExtractor.getNewlyAddedMentions(oldContent, newMentionIds);

            // Then
            assertThat(result).containsExactlyInAnyOrder(789L, 999L);
        }

        @Test
        @DisplayName("모두 기존 멘션이면 빈 배열을 반환한다")
        void getNewlyAddedMentions_AllExisting() {
            // Given
            String oldContent = "안녕 @홍길동[123] @철수[456]";
            Long[] newMentionIds = {123L, 456L};

            // When
            Long[] result = MentionExtractor.getNewlyAddedMentions(oldContent, newMentionIds);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("기존 멘션이 없으면 모든 새 멘션을 반환한다")
        void getNewlyAddedMentions_NoOldMentions() {
            // Given
            String oldContent = "멘션이 없는 댓글입니다";
            Long[] newMentionIds = {123L, 456L};

            // When
            Long[] result = MentionExtractor.getNewlyAddedMentions(oldContent, newMentionIds);

            // Then
            assertThat(result).containsExactlyInAnyOrder(123L, 456L);
        }

        @Test
        @DisplayName("새 멘션이 null이면 빈 배열을 반환한다")
        void getNewlyAddedMentions_NullNewMentions() {
            // Given
            String oldContent = "안녕 @홍길동[123]";
            Long[] newMentionIds = null;

            // When
            Long[] result = MentionExtractor.getNewlyAddedMentions(oldContent, newMentionIds);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("새 멘션이 빈 배열이면 빈 배열을 반환한다")
        void getNewlyAddedMentions_EmptyNewMentions() {
            // Given
            String oldContent = "안녕 @홍길동[123]";
            Long[] newMentionIds = {};

            // When
            Long[] result = MentionExtractor.getNewlyAddedMentions(oldContent, newMentionIds);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("새 멘션 중 일부만 새로운 경우 새로운 것만 반환한다")
        void getNewlyAddedMentions_PartialNew() {
            // Given
            String oldContent = "@사용자1[100] @사용자2[200]";
            Long[] newMentionIds = {100L, 200L, 300L, 400L};

            // When
            Long[] result = MentionExtractor.getNewlyAddedMentions(oldContent, newMentionIds);

            // Then
            assertThat(result).containsExactlyInAnyOrder(300L, 400L);
        }

        @Test
        @DisplayName("중복된 새 멘션은 한 번만 반환한다")
        void getNewlyAddedMentions_DuplicateNewMentions() {
            // Given
            String oldContent = "안녕하세요";
            Long[] newMentionIds = {123L, 123L, 456L};

            // When
            Long[] result = MentionExtractor.getNewlyAddedMentions(oldContent, newMentionIds);

            // Then
            assertThat(result).containsExactlyInAnyOrder(123L, 456L);
        }
    }

    @Nested
    @DisplayName("getNewlyAddedAnonymousMentions 메서드는")
    class GetNewlyAddedAnonymousMentionsTest {

        @Test
        @DisplayName("새롭게 추가된 익명 멘션만 추출한다")
        void getNewlyAddedAnonymousMentions_Success() {
            // Given
            String oldContent = "안녕하세요 @오너십있는 츄러스[-1] 님";
            String[] newAnonymousNicknames = {"오너십있는 츄러스", "도전하는 빙수", "열정적인 아이스크림"};

            // When
            String[] result = MentionExtractor.getNewlyAddedAnonymousMentions(oldContent, newAnonymousNicknames);

            // Then
            assertThat(result).containsExactlyInAnyOrder("도전하는 빙수", "열정적인 아이스크림");
        }

        @Test
        @DisplayName("모두 기존 익명 멘션이면 빈 배열을 반환한다")
        void getNewlyAddedAnonymousMentions_AllExisting() {
            // Given
            String oldContent = "@오너십있는 츄러스[-1] @도전하는 빙수[-1]";
            String[] newAnonymousNicknames = {"오너십있는 츄러스", "도전하는 빙수"};

            // When
            String[] result = MentionExtractor.getNewlyAddedAnonymousMentions(oldContent, newAnonymousNicknames);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("기존 익명 멘션이 없으면 모든 새 익명 멘션을 반환한다")
        void getNewlyAddedAnonymousMentions_NoOldMentions() {
            // Given
            String oldContent = "익명 멘션이 없는 댓글입니다";
            String[] newAnonymousNicknames = {"오너십있는 츄러스", "도전하는 빙수"};

            // When
            String[] result = MentionExtractor.getNewlyAddedAnonymousMentions(oldContent, newAnonymousNicknames);

            // Then
            assertThat(result).containsExactlyInAnyOrder("오너십있는 츄러스", "도전하는 빙수");
        }

        @Test
        @DisplayName("새 익명 멘션이 null이면 빈 배열을 반환한다")
        void getNewlyAddedAnonymousMentions_NullNewMentions() {
            // Given
            String oldContent = "@오너십있는 츄러스[-1]";
            String[] newAnonymousNicknames = null;

            // When
            String[] result = MentionExtractor.getNewlyAddedAnonymousMentions(oldContent, newAnonymousNicknames);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("새 익명 멘션이 빈 배열이면 빈 배열을 반환한다")
        void getNewlyAddedAnonymousMentions_EmptyNewMentions() {
            // Given
            String oldContent = "@오너십있는 츄러스[-1]";
            String[] newAnonymousNicknames = {};

            // When
            String[] result = MentionExtractor.getNewlyAddedAnonymousMentions(oldContent, newAnonymousNicknames);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("새 익명 멘션 중 일부만 새로운 경우 새로운 것만 반환한다")
        void getNewlyAddedAnonymousMentions_PartialNew() {
            // Given
            String oldContent = "@오너십있는 츄러스[-1] 님 안녕하세요";
            String[] newAnonymousNicknames = {"오너십있는 츄러스", "도전하는 빙수", "열정적인 아이스크림"};

            // When
            String[] result = MentionExtractor.getNewlyAddedAnonymousMentions(oldContent, newAnonymousNicknames);

            // Then
            assertThat(result).containsExactlyInAnyOrder("도전하는 빙수", "열정적인 아이스크림");
        }

        @Test
        @DisplayName("중복된 새 익명 멘션은 한 번만 반환한다")
        void getNewlyAddedAnonymousMentions_DuplicateNewMentions() {
            // Given
            String oldContent = "안녕하세요";
            String[] newAnonymousNicknames = {"오너십있는 츄러스", "오너십있는 츄러스", "도전하는 빙수"};

            // When
            String[] result = MentionExtractor.getNewlyAddedAnonymousMentions(oldContent, newAnonymousNicknames);

            // Then
            assertThat(result).containsExactlyInAnyOrder("오너십있는 츄러스", "도전하는 빙수");
        }

        @Test
        @DisplayName("일반 멘션과 익명 멘션이 섞여 있어도 익명 멘션만 추출한다")
        void getNewlyAddedAnonymousMentions_MixedMentions() {
            // Given
            String oldContent = "@홍길동[123] @오너십있는 츄러스[-1] 안녕하세요";
            String[] newAnonymousNicknames = {"오너십있는 츄러스", "도전하는 빙수"};

            // When
            String[] result = MentionExtractor.getNewlyAddedAnonymousMentions(oldContent, newAnonymousNicknames);

            // Then
            assertThat(result).containsExactly("도전하는 빙수");
        }
    }

    @Nested
    @DisplayName("통합 시나리오 테스트")
    class IntegrationTest {

        @Test
        @DisplayName("댓글 수정 시 일반 멘션과 익명 멘션을 모두 처리한다")
        void updateComment_WithBothMentionTypes() {
            // Given
            String oldContent = "@홍길동[123] @오너십있는 츄러스[-1] 님 안녕하세요";
            Long[] newUserMentions = {123L, 456L};
            String[] newAnonymousMentions = {"오너십있는 츄러스", "도전하는 빙수"};

            // When
            Long[] newUsers = MentionExtractor.getNewlyAddedMentions(oldContent, newUserMentions);
            String[] newAnonymous = MentionExtractor.getNewlyAddedAnonymousMentions(oldContent, newAnonymousMentions);

            // Then
            assertThat(newUsers).containsExactly(456L);
            assertThat(newAnonymous).containsExactly("도전하는 빙수");
        }

        @Test
        @DisplayName("모든 멘션이 새로운 경우 모두 반환한다")
        void updateComment_AllNewMentions() {
            // Given
            String oldContent = "멘션이 없는 댓글";
            Long[] newUserMentions = {123L, 456L};
            String[] newAnonymousMentions = {"오너십있는 츄러스", "도전하는 빙수"};

            // When
            Long[] newUsers = MentionExtractor.getNewlyAddedMentions(oldContent, newUserMentions);
            String[] newAnonymous = MentionExtractor.getNewlyAddedAnonymousMentions(oldContent, newAnonymousMentions);

            // Then
            assertThat(newUsers).containsExactlyInAnyOrder(123L, 456L);
            assertThat(newAnonymous).containsExactlyInAnyOrder("오너십있는 츄러스", "도전하는 빙수");
        }

        @Test
        @DisplayName("모든 멘션이 기존 것이면 빈 배열을 반환한다")
        void updateComment_AllExistingMentions() {
            // Given
            String oldContent = "@홍길동[123] @철수[456] @오너십있는 츄러스[-1] @도전하는 빙수[-1]";
            Long[] newUserMentions = {123L, 456L};
            String[] newAnonymousMentions = {"오너십있는 츄러스", "도전하는 빙수"};

            // When
            Long[] newUsers = MentionExtractor.getNewlyAddedMentions(oldContent, newUserMentions);
            String[] newAnonymous = MentionExtractor.getNewlyAddedAnonymousMentions(oldContent, newAnonymousMentions);

            // Then
            assertThat(newUsers).isEmpty();
            assertThat(newAnonymous).isEmpty();
        }
    }
}
