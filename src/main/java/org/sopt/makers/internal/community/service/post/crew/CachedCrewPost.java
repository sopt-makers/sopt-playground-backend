package org.sopt.makers.internal.community.service.post.crew;

import java.time.LocalDateTime;
import java.util.List;

import org.sopt.makers.internal.external.makers.CrewPost;

public record CachedCrewPost(
	long id,
	String title,
	String contents,
	LocalDateTime createdDate,
	List<String> images,
	long userId,
	long orgId,
	String userName,
	String profileImage,
	String part,
	int generation,
	int likeCount,
	boolean liked,
	int viewCount,
	int commentCount,
	Long meetingId
) {
	public static CachedCrewPost from(CrewPost crewPost) {
		CrewPost.CrewUser crewUser = crewPost.user();
		CrewPost.PartInfo partInfo = crewUser.partInfo();

		return new CachedCrewPost(
			crewPost.id(),
			crewPost.title(),
			crewPost.contents(),
			crewPost.createdDate(),
			crewPost.images() == null ? List.of() : crewPost.images(),
			crewUser.id(),
			crewUser.orgId(),
			crewUser.name(),
			crewUser.profileImage(),
			partInfo.part(),
			partInfo.generation(),
			crewPost.likeCount(),
			crewPost.isLiked(),
			crewPost.viewCount(),
			crewPost.commentCount(),
			crewPost.meetingId()
		);
	}

	public CrewPost toCrewPost() {
		return new CrewPost(
			id,
			title,
			contents,
			createdDate,
			images == null ? List.of() : images,
			new CrewPost.CrewUser(
				userId,
				orgId,
				userName,
				profileImage,
				new CrewPost.PartInfo(part, generation)
			),
			likeCount,
			liked,
			viewCount,
			commentCount,
			meetingId
		);
	}
}