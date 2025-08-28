package org.sopt.makers.internal.external.platform;

import java.util.List;

public record UserSearchResponse(
	List<InternalUserDetails> profiles,
	boolean hasNext,
	int totalCount
) {
}
