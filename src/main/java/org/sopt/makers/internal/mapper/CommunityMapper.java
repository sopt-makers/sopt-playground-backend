package org.sopt.makers.internal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.sopt.makers.internal.domain.CommunityComment;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.MemberSoptActivity;
import org.sopt.makers.internal.domain.community.CommunityPost;
import org.sopt.makers.internal.dto.community.CategoryPostMemberDao;
import org.sopt.makers.internal.dto.community.PostResponse;
import org.sopt.makers.internal.dto.internal.*;
import org.sopt.makers.internal.dto.member.*;
import org.sopt.makers.internal.dto.wordChainGame.WordChainGameGenerateResponse;
import org.sopt.makers.internal.dto.wordChainGame.WordChainGameRoomResponse;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommunityMapper {

//    @Mapping()
//    PostResponse toPostResponse(CategoryPostMemberDao categoryPostMemberDao, )
    @Mapping(target = "comments", source = "comments")
    PostResponse toResponse(CommunityPost post, Long writerId, List<CommunityComment> comments);
}
