package org.sopt.makers.internal.internal;

import java.util.List;
import lombok.RequiredArgsConstructor;
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

    @Transactional(readOnly = true)
    public List<Project> fetchAll () {
        return projectRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ProjectLinkDao> fetchAllLinks () {
        return projectQueryRepository.findAllLinks();
    }

//    @Transactional(readOnly = true)
//    public List<ProjectMemberVo> fetchById (Long id) {
//        val project = projectQueryRepository.findById(id);
//        if (project.isEmpty()) throw new NotFoundDBEntityException("잘못된 프로젝트 조회입니다.");
//
//        val projectMemberIds = project.stream()
//                .filter(ProjectMemberDao::memberHasProfile)
//                .map(ProjectMemberDao::memberId)
//                .collect(Collectors.toList());
//        val memberActivityMap = soptActivityRepository.findAllByMemberIdIn(projectMemberIds)
//                .stream().collect(Collectors.groupingBy(MemberSoptActivity::getMemberId, Collectors.toList()));
//        val memberGenerationMap = memberActivityMap.entrySet().stream().collect(Collectors.toMap(
//                Map.Entry::getKey, e -> e.getValue().stream().map(MemberSoptActivity::getGeneration).collect(Collectors.toList())
//        ));
//        return project.stream().map(p -> projectMapper.projectMemberDaoToProjectMemberVo(
//                p, memberGenerationMap.getOrDefault(p.memberId(), List.of())
//        )).collect(Collectors.toList());
//    }
//    @Transactional(readOnly = true)
//    public Member getMemberById (Long id) {
//        return memberRepository.findById(id).orElseThrow(() -> new NotFoundDBEntityException("Member"));
//    }
//
//    @Transactional(readOnly = true)
//    public Member getMemberHasProfileById (Long id) {
//        Member member = memberRepository.findById(id)
//                .orElseThrow(() -> new NotFoundDBEntityException("해당 id의 Member를 찾을 수 없습니다."));
//        if (member.getHasProfile()) return member;
//        else throw new MemberHasNotProfileException("해당 Member는 프로필이 없습니다.");
//    }
//
//    @Transactional(readOnly = true)
//    public List<MemberProfileProjectDao> getMemberProfileProjects (Long id) {
//        return memberProfileQueryRepository.findMemberProfileProjectsByMemberId(id);
//    }
//
//    @Transactional(readOnly = true)
//    public List<MemberSoptActivity> getMemberActivities (Long id) {
//        return soptActivityRepository.findAllByMemberId(id);
//    }
//
//    @Transactional(readOnly = true)
//    public Integer getMemberLatestActivityGeneration (Long id) {
//        return soptActivityRepository.findAllByMemberId(id).stream().map(MemberSoptActivity::getGeneration)
//                .max(Integer::compare).orElseThrow(() -> new NotFoundDBEntityException("해당 id의 Member Activity를 찾을 수 없습니다."));
//    }
//
//    @Transactional(readOnly = true)
//    public String getMemberLatestActivityPart (Long id) {
//        return soptActivityRepository.findAllByMemberId(id).stream()
//                .max(Comparator.comparingInt(MemberSoptActivity::getGeneration))
//                .map(MemberSoptActivity::getPart)
//                .orElseThrow(() -> new NotFoundDBEntityException("해당 id의 Member Activity를 찾을 수 없습니다."));
//    }
//
//    public Map<String, List<ActivityVo>> getMemberProfileActivity (
//            List<MemberSoptActivity> memberActivities,
//            List<MemberProfileProjectDao> memberProfileProjects
//    ) {
//        val cardinalInfoMap = memberActivities.stream()
//                .collect(Collectors.toMap(
//                        MemberSoptActivity::getGeneration,
//                        MemberSoptActivity::getPart,
//                        (p1, p2) -> p1)
//                );
//        val activities = memberActivities.stream().map(a -> memberMapper.toActivityInfoVo(a, false));
//        val projects = memberProfileProjects.stream()
//                .filter(p -> p.generation() != null)
//                .map(p -> {
//                    val part = cardinalInfoMap.getOrDefault(p.generation(), "");
//                    return memberMapper.toActivityInfoVo(p, true, part);
//                });
//        val genActivityMap = Stream.concat(activities, projects)
//                .collect(Collectors.groupingBy(ActivityVo::generation));
//        return genActivityMap.entrySet().stream()
//                .collect(Collectors.toMap(
//                        e -> e.getKey() + "," + cardinalInfoMap.getOrDefault(e.getKey(), ""),
//                        Map.Entry::getValue
//                ));
//    }
//
//    @Transactional(readOnly = true)
//    public List<Member> getMemberProfiles(Integer filter, Integer limit, Integer cursor, String name, Integer generation) {
//        val part = getMemberPart(filter);
//        if(limit != null) {
//            return memberProfileQueryRepository.findAllLimitedMemberProfile(
//                    part, limit, cursor, name, generation,
//                    null, null, null, null
//            );
//        }
//        else {
//            return memberProfileQueryRepository.findAllMemberProfile(
//                    part, name, generation,
//                    null, null, null, null
//            );
//        }
//    }
//
//    @Transactional(readOnly = true)
//    public int getMemberCountByGeneration (Integer generation) {
//        return memberProfileQueryRepository.countMembersByGeneration(generation);
//    }
//
//    @Transactional(readOnly = true)
//    public List<Long> getMembersIdByGeneration (Integer generation) {
//        return memberProfileQueryRepository.findAllMemberIdsByGeneration(generation);
//    }
//
//    @Transactional(readOnly = true)
//    public List<Long> getMembersIdByRecommendFilter (List<Integer> generations, String university, String mbti) {
//        return memberProfileQueryRepository.findAllMemberIdsByRecommendFilter(generations, university, mbti);
//    }
//
//    @Transactional(readOnly = true)
//    public List<Long> getInactivityMemberIdListByGenerationAndPart(Integer generation, Part part) {
//        return memberProfileQueryRepository.findAllInactivityMemberIdsByGenerationAndPart(generation, part);
//    }
//
//    public String getPartName(Integer partFilter) {
//        return getMemberPart(partFilter);
//    }
//
//    private String getMemberPart (Integer filter) {
//        if (filter == null) return null;
//        return switch (filter) {
//            case 1 -> "기획";
//            case 2 -> "디자인";
//            case 3 -> "웹";
//            case 4 -> "서버";
//            case 5 -> "안드로이드";
//            case 6 -> "iOS";
//            default -> null;
//        };
//    }
//
//    public InternalAuthVo authByToken (String previousAccessToken, String serviceName) {
//        val isVerified = internalTokenManager.verifyAuthToken(previousAccessToken);
//        if (!isVerified) return new InternalAuthVo(null, "invalidToken");
//        val userId = Long.parseLong(internalTokenManager.getUserIdFromAuthToken(previousAccessToken));
//        val accessToken = internalTokenManager.createAuthToken(userId ,30, serviceName);
//        return new InternalAuthVo(accessToken, null);
//    }
}
