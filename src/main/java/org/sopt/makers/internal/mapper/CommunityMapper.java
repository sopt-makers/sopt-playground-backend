package org.sopt.makers.internal.mapper;

import org.mapstruct.Mapper;
import org.sopt.makers.internal.domain.community.CommunityComment;
import org.sopt.makers.internal.community.domain.CommunityPost;
import org.sopt.makers.internal.domain.community.DeletedCommunityComment;
import org.sopt.makers.internal.domain.community.DeletedCommunityPost;

@Mapper(componentModel = "spring")
public interface CommunityMapper {
    DeletedCommunityComment toDeleteCommunityComment (CommunityComment comment);
    DeletedCommunityPost toDeleteCommunityPost (CommunityPost post);
}
