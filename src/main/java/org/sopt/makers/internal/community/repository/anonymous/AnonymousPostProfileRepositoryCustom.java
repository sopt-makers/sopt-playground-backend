package org.sopt.makers.internal.community.repository.anonymous;

import org.sopt.makers.internal.community.domain.anonymous.AnonymousPostProfile;

import java.util.List;

public interface AnonymousPostProfileRepositoryCustom {

    List<AnonymousPostProfile> findTopByOrderByIdDescWithLimit(int limit);
}
