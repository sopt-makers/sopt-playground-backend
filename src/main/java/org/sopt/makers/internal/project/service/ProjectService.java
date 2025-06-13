package org.sopt.makers.internal.project.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.member.domain.MemberSoptActivity;
import org.sopt.makers.internal.project.domain.ProjectLink;
import org.sopt.makers.internal.project.domain.MemberProjectRelation;
import org.sopt.makers.internal.project.domain.Project;
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.project.mapper.ProjectMapper;
import org.sopt.makers.internal.member.repository.soptactivity.MemberSoptActivityRepository;
import org.sopt.makers.internal.project.dto.request.ProjectSaveRequest;
import org.sopt.makers.internal.project.dto.request.ProjectUpdateRequest;
import org.sopt.makers.internal.project.dto.response.ProjectLinkDao;
import org.sopt.makers.internal.project.dto.response.ProjectMemberDao;
import org.sopt.makers.internal.project.dto.response.ProjectMemberVo;
import org.sopt.makers.internal.project.repository.MemberProjectRelationRepository;
import org.sopt.makers.internal.project.repository.ProjectLinkRepository;
import org.sopt.makers.internal.project.repository.ProjectQueryRepository;
import org.sopt.makers.internal.project.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectLinkRepository projectLinkRepository;
    private final MemberProjectRelationRepository relationRepository;
    private final ProjectQueryRepository projectQueryRepository;
    private final MemberSoptActivityRepository soptActivityRepository;
    private final ProjectMapper projectMapper;

    @Transactional
    public void createProject (ProjectSaveRequest request) {
        val project = projectRepository.save(
                Project.builder()
                        .name(request.name())
                        .writerId(request.writerId())
                        .generation(request.generation())
                        .category(request.category())
                        .startAt(request.startAt())
                        .endAt(request.endAt())
                        .serviceType(request.serviceType())
                        .isAvailable(request.isAvailable())
                        .isFounding(request.isFounding())
                        .summary(request.summary())
                        .detail(request.detail())
                        .logoImage(request.logoImage())
                        .thumbnailImage(request.thumbnailImage())
                        .images(request.images())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );

        relationRepository.saveAll(request.members().stream().map(memberRequest -> MemberProjectRelation.builder()
                .projectId(project.getId())
                .userId(memberRequest.memberId())
                .role(memberRequest.memberRole())
                .description(memberRequest.memberDescription())
                .isTeamMember(memberRequest.isTeamMember())
                .build()).collect(Collectors.toList()));

        projectLinkRepository.saveAll(request.links().stream().map(linkRequest -> ProjectLink.builder()
                .projectId(project.getId())
                .title(linkRequest.linkTitle())
                .url(linkRequest.linkUrl())
                .build()).collect(Collectors.toList()));
    }

    @Transactional
    public void updateProject (Long writerId, Long id, ProjectUpdateRequest request) {
        val project = projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundDBEntityException("잘못된 프로젝트 조회입니다."));
        if (!Objects.equals(project.getWriterId(), writerId)) throw new ClientBadRequestException("수정 권한이 없는 유저입니다.");
        project.updateAll(
                request.name(), request.generation(), request.category(), request.startAt(),
                request.endAt(), request.serviceType(), request.isAvailable(), request.isFounding(), request.summary(), request.detail(),
                request.logoImage(), request.thumbnailImage(), request.images()
        );

        val relationList = relationRepository.findAllByProjectId(id);
        val relations = relationList.stream()
                .collect(Collectors.toMap(MemberProjectRelation::getUserId, Function.identity()));
        val relationUserSet = Set.copyOf(relationList.stream().map(MemberProjectRelation::getUserId).collect(Collectors.toList()));
        val requestedRelationUserSet = Set.copyOf(request.members().stream()
                .map(ProjectUpdateRequest.ProjectMemberUpdateRequest::memberId)
                .toList());

        relationRepository.deleteAll(relationUserSet.stream()
                .filter(e -> !requestedRelationUserSet.contains(e))
                .map(relations::get)
                .collect(Collectors.toList())
        );

        relationRepository.saveAll(request.members().stream().map(memberRequest -> {
            if (relations.containsKey(memberRequest.memberId())) {
                return relations.get(memberRequest.memberId())
                        .updateAll(
                                memberRequest.memberRole(),
                                memberRequest.memberDescription(),
                                memberRequest.isTeamMember()
                        );
            } else {
                return MemberProjectRelation.builder()
                        .projectId(project.getId())
                        .userId(memberRequest.memberId())
                        .role(memberRequest.memberRole())
                        .description(memberRequest.memberDescription())
                        .isTeamMember(memberRequest.isTeamMember())
                        .build();
            }
        }).collect(Collectors.toList()));

        projectLinkRepository.deleteAllByProjectId(id);
        projectLinkRepository.saveAll(request.links().stream().map(linkRequest -> ProjectLink.builder()
                .projectId(project.getId())
                .title(linkRequest.linkTitle())
                .url(linkRequest.linkUrl())
                .build()).collect(Collectors.toList()));

    }

    @Transactional
    public void deleteProject (Long writerId, Long id) {
        val project = projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundDBEntityException("잘못된 프로젝트 조회입니다."));
        if (!Objects.equals(project.getWriterId(), writerId)) throw new ClientBadRequestException("수정 권한이 없는 유저입니다.");
        projectLinkRepository.deleteAllByProjectId(id);
        relationRepository.deleteAllByProjectId(id);
        projectRepository.delete(project);
    }

    @Transactional(readOnly = true)
    public List<Project> getProjectByName (String name) {
        return projectRepository.findAllByNameContaining(name);
    }

    @Transactional(readOnly = true)
    public List<ProjectLinkDao> fetchAllLinks () {
        return projectQueryRepository.findAllLinks();
    }

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
    public List<ProjectLinkDao> fetchLinksById (Long id) {
        val project = projectQueryRepository.findLinksById(id);
        return project;
    }

    @Transactional(readOnly = true)
    public List<Project> fetchAll (Integer limit, Long cursor, String name, String category, Boolean isAvailable, Boolean isFounding) {
        if(limit != null && name != null) return projectQueryRepository.findAllLimitedProjectsContainsName(limit, cursor, name, category, isAvailable, isFounding);
        else if(limit != null) {
            return projectQueryRepository.findAllLimitedProjects(limit, cursor, category, isAvailable, isFounding);
        } else if(name != null) {
            return projectQueryRepository.findAllNameProjects(name, category, isAvailable, isFounding);
        }
        return projectRepository.findAll();
    }

    @Transactional(readOnly = true)
    public int getProjectsCount(String name, String category, Boolean isAvailable, Boolean isFounding) {
        return projectQueryRepository.countAllProjects(name, category, isAvailable, isFounding);
    }

    @Transactional(readOnly = true)
    public int getProjectCountByMemberId(Long memberId) {
        return projectQueryRepository.countProjectsExcludeSopkathon(memberId);
    }

    @Transactional(readOnly = true)
    public Long getAllCount() {
        return projectRepository.count();
    }
}
