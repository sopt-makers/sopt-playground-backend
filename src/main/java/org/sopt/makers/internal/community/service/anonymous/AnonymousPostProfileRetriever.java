package org.sopt.makers.internal.community.service.anonymous;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousPostProfile;
import org.sopt.makers.internal.community.repository.anonymous.AnonymousPostProfileRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnonymousPostProfileRetriever {

    private final AnonymousPostProfileRepository anonymousPostProfileRepository;

    public List<AnonymousPostProfile> getTopByOrderByCreatedAt(int limit) {

        return anonymousPostProfileRepository.findTopByOrderByIdDescWithLimit(limit);
    }
}
