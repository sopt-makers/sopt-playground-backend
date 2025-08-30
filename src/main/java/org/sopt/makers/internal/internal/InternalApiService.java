package org.sopt.makers.internal.internal;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.PlatformService;
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

    @Transactional(readOnly = true)
    public List<Project> fetchAll () {
        return projectRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ProjectLinkDao> fetchAllLinks () {
        return projectQueryRepository.findAllLinks();
    }

    @Transactional(readOnly = true)
    public List<Long> getMembersIdByRecommendFilter (List<Integer> generations, String university, String mbti) {
        if (generations == null || generations.isEmpty()) {
            throw new IllegalArgumentException("generation 필터는 비어 있을 수 없습니다.");
        }

        List<Long> userIds =  memberProfileQueryRepository.findAllMemberIdsByRecommendFilter(university, mbti);
        if (userIds.isEmpty()) return List.of();

        List<InternalUserDetails> internalUserDetails = platformService.getInternalUsers(userIds);

        return internalUserDetails.stream()
                .filter(user -> user.soptActivities().stream()
                        .anyMatch(activity -> generations.contains(activity.generation()))
                )
                .map(InternalUserDetails::userId)
                .toList();
    }
}
