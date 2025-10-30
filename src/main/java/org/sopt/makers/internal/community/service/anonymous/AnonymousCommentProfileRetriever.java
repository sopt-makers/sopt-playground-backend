package org.sopt.makers.internal.community.service.anonymous;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousCommentProfile;
import org.sopt.makers.internal.community.repository.anonymous.AnonymousCommentProfileRepository;
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class AnonymousCommentProfileRetriever {

    private final AnonymousCommentProfileRepository anonymousCommentProfileRepository;

    public AnonymousCommentProfile findByCommunityCommentId(Long commentId) {
        return anonymousCommentProfileRepository.findByCommunityCommentId(commentId).orElse(null);
    }

    public List<AnonymousCommentProfile> findAllByPostId(Long postId) {
        return anonymousCommentProfileRepository.findAllByCommunityCommentPostId(postId);
    }

    public void validateAnonymousNicknamesInPost(Long postId, String[] nicknames) {
        if (nicknames == null || nicknames.length == 0) {
            return;
        }

        List<String> nicknameList = Arrays.asList(nicknames);
        List<String> foundNicknames = anonymousCommentProfileRepository
                .findNicknamesByPostIdAndNicknamesIn(postId, nicknameList);

        Set<String> foundNicknameSet = Set.copyOf(foundNicknames);

        for (String nickname : nicknames) {
            if (!foundNicknameSet.contains(nickname)) {
                throw new ClientBadRequestException(
                        "해당 게시글에 존재하지 않는 익명 닉네임입니다: " + nickname
                );
            }
        }
    }
}
