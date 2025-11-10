package org.sopt.makers.internal.community.service.comment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousProfile;
import org.sopt.makers.internal.community.domain.comment.CommunityComment;
import org.sopt.makers.internal.community.repository.comment.CommunityCommentRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommentMentionAnonymizer {

    private final CommunityCommentRepository communityCommentRepository;

    public void anonymizeMentionsInReplies(CommunityComment deletedComment) {
        List<CommunityComment> replies = communityCommentRepository.findAllByParentCommentId(deletedComment.getId());

        if (replies.isEmpty()) {
            return;
        }

        for (CommunityComment reply : replies) {
            String updatedContent = anonymizeMentionInContent(
                    reply.getContent(),
                    deletedComment.getWriterId(),
                    deletedComment.getAnonymousProfile()
            );

            if (!updatedContent.equals(reply.getContent())) {
                reply.updateContent(updatedContent);
                communityCommentRepository.save(reply);
            }
        }
    }

    private String anonymizeMentionInContent(
            String content,
            Long deletedUserId,
            AnonymousProfile deletedAnonymousProfile
    ) {
        String result = content;

        // 실명 사용자 언급 익명화: @이름[userId] -> @_
        String realNamePattern = "@.*?\\[" + deletedUserId + "\\]";
        result = result.replaceAll(realNamePattern, "@_");

        // 익명 사용자 언급 익명화: @익명닉네임[-1] -> @_
        if (deletedAnonymousProfile != null && deletedAnonymousProfile.getNickname() != null) {
            String anonymousNickname = deletedAnonymousProfile.getNickname().getNickname();
            String anonymousPattern = "@" + Pattern.quote(anonymousNickname) + "\\[-1\\]";
            result = result.replaceAll(anonymousPattern, "@_");
        }

        return result;
    }
}
