package org.sopt.makers.internal.mapper;

import lombok.val;
import org.sopt.makers.internal.dto.community.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommunityResponseMapper {

    public CommentResponse toCommentResponse(CommentDao dao) {
        return new CommentResponse(dao.id(), dao.userId(), dao.userName(), dao.profileImage(), dao.activities(),
                dao.careers(), dao.content(), dao.isBlindWriter(), dao.createdAt(), dao.updatedAt());
    }

    public CommunityMemberResponse toMemberResponse(CategoryPostMemberDao dao) {
        return new CommunityMemberResponse(dao.userId(), dao.userName(), dao.profileImage(),dao.activities(), dao.careers());
    }

    public PostResponse toPostResponse (CategoryPostMemberDao dao, List<CommentDao> commentDaos) {
        val member = toMemberResponse(dao);
        val comments = commentDaos.stream().map(this::toCommentResponse).collect(Collectors.toList());
        return new PostResponse(dao.id(), member, dao.userId(), dao.title(), dao.content(), dao.hits(), dao.images(),
                dao.isQuestion(), dao.isBlindWriter(), dao.createdAt(), comments);
    }
}
