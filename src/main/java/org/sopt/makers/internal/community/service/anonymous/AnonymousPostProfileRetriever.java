package org.sopt.makers.internal.community.service.anonymous;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.repository.anonymous.AnonymousPostProfileRepository;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousPostProfile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AnonymousPostProfileRetriever {

    private final AnonymousPostProfileRepository anonymousPostProfileRepository;

    public List<AnonymousPostProfile> getTopByOrderByCreatedAt(int limit) {

        return anonymousPostProfileRepository.findTopByOrderByIdDescWithLimit(limit);
    }
}
