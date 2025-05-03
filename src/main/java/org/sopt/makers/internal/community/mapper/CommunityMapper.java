package org.sopt.makers.internal.community.mapper;

import org.mapstruct.Mapper;
import org.sopt.makers.internal.community.domain.comment.CommunityComment;
import org.sopt.makers.internal.community.domain.CommunityPost;
import org.sopt.makers.internal.community.domain.comment.DeletedCommunityComment;
import org.sopt.makers.internal.community.domain.DeletedCommunityPost;

@Mapper(componentModel = "spring")
public interface CommunityMapper {
    DeletedCommunityComment toDeleteCommunityComment (CommunityComment comment);
    DeletedCommunityPost toDeleteCommunityPost (CommunityPost post);
}
