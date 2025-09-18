package org.sopt.makers.internal.internal;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.PlatformService;
import org.sopt.makers.internal.external.platform.UserSearchResponse;
import org.sopt.makers.internal.member.repository.MemberProfileQueryRepository;
import org.sopt.makers.internal.project.domain.Project;
import org.sopt.makers.internal.project.dto.dao.ProjectLinkDao;
import org.sopt.makers.internal.project.repository.ProjectQueryRepository;
import org.sopt.makers.internal.project.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class InternalApiService {
    private final ProjectRepository projectRepository;
    private final ProjectQueryRepository projectQueryRepository;
    private final MemberProfileQueryRepository memberProfileQueryRepository;

    private final PlatformService platformService;

    private final static int PAGE_SIZE = 200;

    @Transactional(readOnly = true)
    public List<Project> fetchAll () {
        return projectRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ProjectLinkDao> fetchAllLinks () {
        return projectQueryRepository.findAllLinks();
    }

    @Transactional(readOnly = true)
    public Set<Long> getMemberIdsByRecommendFilter (List<Integer> generations, String university, String mbti) {
        System.out.println(mbti);
        if (generations == null || generations.isEmpty()) {
            throw new IllegalArgumentException("generation 필터는 비어 있을 수 없습니다.");
        }

        List<Long> userIds =  memberProfileQueryRepository.findAllMemberIdsByRecommendFilter(university, mbti);
        System.out.println(userIds);
        if (userIds.isEmpty()) return Set.of();

        Set<Long> generationUserIds = new HashSet<>();
        for (int generation : generations) {
            UserSearchResponse response = platformService.searchInternalUsers(generation, null, null, null, PAGE_SIZE, 0, null);

            List<InternalUserDetails> profiles = response.profiles();
            if (profiles == null || profiles.isEmpty()) break;

            for (InternalUserDetails userInfo : profiles) {
                generationUserIds.add(userInfo.userId());
            }
        }

        Set<Long> result = new HashSet<>(userIds);
        result.retainAll(generationUserIds);
        return result;
    }
}
