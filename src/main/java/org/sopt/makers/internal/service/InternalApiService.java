package org.sopt.makers.internal.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.MemberSoptActivity;
import org.sopt.makers.internal.domain.Project;
import org.sopt.makers.internal.dto.member.ActivityVo;
import org.sopt.makers.internal.dto.member.MemberProfileProjectDao;
import org.sopt.makers.internal.dto.project.ProjectLinkDao;
import org.sopt.makers.internal.dto.project.ProjectMemberDao;
import org.sopt.makers.internal.dto.project.ProjectMemberVo;
import org.sopt.makers.internal.exception.MemberHasNotProfileException;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.mapper.MemberMapper;
import org.sopt.makers.internal.mapper.ProjectMapper;
import org.sopt.makers.internal.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class InternalApiService {
    private final ProjectRepository projectRepository;
    private final ProjectQueryRepository projectQueryRepository;
    private final MemberSoptActivityRepository soptActivityRepository;
    private final ProjectMapper projectMapper;
    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;
    private final MemberProfileQueryRepository memberProfileQueryRepository;

    @Transactional(readOnly = true)
    public List<ProjectMemberVo> fetchById (Long id) {
        val project = projectQueryRepository.findById(id);
        if (project.isEmpty()) throw new NotFoundDBEntityException("잘못된 프로젝트 조회입니다.");

        val projectMemberIds = project.stream()
                .filter(ProjectMemberDao::memberHasProfile)
                .map(ProjectMemberDao::memberId)
                .collect(Collectors.toList());
        val memberActivityMap = soptActivityRepository.findAllByMemberIdIn(projectMemberIds)
                .stream().collect(Collectors.groupingBy(MemberSoptActivity::getMemberId, Collectors.toList()));
        val memberGenerationMap = memberActivityMap.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey, e -> e.getValue().stream().map(MemberSoptActivity::getGeneration).collect(Collectors.toList())
        ));
        return project.stream().map(p -> projectMapper.projectMemberDaoToProjectMemberVo(
                p, memberGenerationMap.getOrDefault(p.memberId(), List.of())
        )).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Project> fetchAll () {
        return projectRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ProjectLinkDao> fetchAllLinks () {
        return projectQueryRepository.findAllLinks();
    }

    @Transactional(readOnly = true)
    public List<ProjectLinkDao> fetchLinksById (Long id) {
        val project = projectQueryRepository.findLinksById(id);
        return project;
    }

    @Transactional(readOnly = true)
    public Member getMemberById (Long id) {
        return memberRepository.findById(id).orElseThrow(() -> new NotFoundDBEntityException("Member"));
    }

    @Transactional(readOnly = true)
    public Member getMemberHasProfileById (Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundDBEntityException("해당 id의 Member를 찾을 수 없습니다."));
        if (member.getHasProfile()) return member;
        else throw new MemberHasNotProfileException("해당 Member는 프로필이 없습니다.");
    }

    @Transactional(readOnly = true)
    public List<MemberProfileProjectDao> getMemberProfileProjects (Long id) {
        return memberProfileQueryRepository.findMemberProfileProjectsByMemberId(id);
    }

    public Map<String, List<ActivityVo>> getMemberProfileActivity (
            List<MemberSoptActivity> memberActivities,
            List<MemberProfileProjectDao> memberProfileProjects
    ) {
        val cardinalInfoMap = memberActivities.stream()
                .collect(Collectors.toMap(
                        MemberSoptActivity::getGeneration,
                        MemberSoptActivity::getPart,
                        (p1, p2) -> p1)
                );
        val activities = memberActivities.stream().map(a -> memberMapper.toActivityInfoVo(a, false));
        val projects = memberProfileProjects.stream()
                .filter(p -> p.generation() != null)
                .map(p -> {
                    val part = cardinalInfoMap.getOrDefault(p.generation(), "");
                    return memberMapper.toActivityInfoVo(p, true, part);
                });
        val genActivityMap = Stream.concat(activities, projects)
                .collect(Collectors.groupingBy(ActivityVo::generation));
        return genActivityMap.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey() + "," + cardinalInfoMap.getOrDefault(e.getKey(), ""),
                        Map.Entry::getValue
                ));
    }

    @Transactional(readOnly = true)
    public List<Member> getMemberProfiles(Integer filter, Integer limit, Integer cursor, String name, Integer generation) {
        val part = getMemberPart(filter);
        if (name == null) {
            if (part != null && cursor != null && limit != null && generation != null)
                return memberProfileQueryRepository.findAllLimitedMemberProfileByPartAndGeneration(part, limit, cursor, generation);
            if (generation != null && cursor != null && limit != null)
                return memberProfileQueryRepository.findAllLimitedMemberProfileByGeneration(generation, limit, cursor);
            if (part != null && cursor != null && limit != null)
                return memberProfileQueryRepository.findAllLimitedMemberProfileByPart(part, limit, cursor);
            if (part != null && generation != null)
                return memberProfileQueryRepository.findAllMemberProfileByPartAndGeneration(part, generation);
            if (part != null)
                return memberProfileQueryRepository.findAllMemberProfileByPart(part);
            if (generation != null)
                return memberProfileQueryRepository.findAllMemberProfileByGeneration(generation);
            if (limit != null && cursor != null)
                return memberProfileQueryRepository.findAllLimitedMemberProfile(limit, cursor);
        } else {
            if (part != null && cursor != null && limit != null && generation != null)
                return memberProfileQueryRepository.findAllLimitedMemberProfileByPartAndNameAndGeneration(part, limit, cursor, name, generation);
            if (part != null && cursor != null && limit != null)
                return memberProfileQueryRepository.findAllLimitedMemberProfileByPartAndName(part, limit, cursor, name);
            if (generation != null && cursor != null && limit != null)
                return memberProfileQueryRepository.findAllLimitedMemberProfileByGenerationAndName(generation, limit, cursor, name);
            if (part != null)
                return memberProfileQueryRepository.findAllMemberProfileByPartAndName(part, name);
            if (generation != null)
                return memberProfileQueryRepository.findAllMemberProfileByGenerationAndName(generation, name);
            if (limit != null && cursor != null)
                return memberProfileQueryRepository.findAllLimitedMemberProfileByName(limit, cursor, name);
            return memberProfileQueryRepository.findAllByName(name);
        }
        return memberRepository.findAllByHasProfileTrue();
    }

    private String getMemberPart (Integer filter) {
        if (filter == null) return null;
        return switch (filter) {
            case 1 -> "기획";
            case 2 -> "디자인";
            case 3 -> "웹";
            case 4 -> "서버";
            case 5 -> "안드로이드";
            case 6 -> "iOS";
            default -> null;
        };
    }
}
