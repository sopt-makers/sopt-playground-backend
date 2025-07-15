package org.sopt.makers.internal.project.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.auth.AuthConfig;
import org.sopt.makers.internal.exception.WrongImageInputException;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.PlatformClient;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.MemberSoptActivity;
import org.sopt.makers.internal.member.repository.MemberRepository;
import org.sopt.makers.internal.project.domain.ProjectLink;
import org.sopt.makers.internal.project.domain.MemberProjectRelation;
import org.sopt.makers.internal.project.domain.Project;
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.project.dto.response.*;
import org.sopt.makers.internal.project.mapper.ProjectMapper;
import org.sopt.makers.internal.member.repository.soptactivity.MemberSoptActivityRepository;
import org.sopt.makers.internal.project.dto.request.ProjectSaveRequest;
import org.sopt.makers.internal.project.dto.request.ProjectUpdateRequest;
import org.sopt.makers.internal.project.mapper.ProjectResponseMapper;
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
    private final MemberProjectRelationRepository memberProjectRelationRepository;
    private final ProjectQueryRepository projectQueryRepository;
    private final MemberSoptActivityRepository soptActivityRepository;
    private final MemberRepository memberRepository;

    private final ProjectMapper projectMapper;
    private final ProjectResponseMapper projectResponseMapper;

    private final PlatformClient platformClient;
    private final AuthConfig authConfig;

    @Transactional
    public void createProject (ProjectSaveRequest request) {
        validateImageCount(request.images().length);
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

        memberProjectRelationRepository.saveAll(request.members().stream().map(memberRequest -> MemberProjectRelation.builder()
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
    public void updateProject (Long writerId, Long projectId, ProjectUpdateRequest request) {
        validateImageCount(request.images().length);

        Project project = getProjectById(projectId);
        validateWriter(project, writerId);

        project.updateAll(
                request.name(), request.generation(), request.category(), request.startAt(),
                request.endAt(), request.serviceType(), request.isAvailable(), request.isFounding(), request.summary(), request.detail(),
                request.logoImage(), request.thumbnailImage(), request.images()
        );

        updateProjectMembers(projectId, request);
        updateProjectLinks(projectId, request);
    }

    private void updateProjectMembers(Long projectId, ProjectUpdateRequest request) {
        List<MemberProjectRelation> existingRelations = memberProjectRelationRepository.findAllByProjectId(projectId);
        Map<Long, MemberProjectRelation> relationMap = existingRelations.stream()
                .collect(Collectors.toMap(MemberProjectRelation::getUserId, Function.identity()));

        Set<Long> requestedUserIds = request.members().stream()
                .map(ProjectUpdateRequest.ProjectMemberUpdateRequest::memberId)
                .collect(Collectors.toSet());

        List<MemberProjectRelation> relationsToRemove = relationMap.keySet().stream()
                .filter(id -> !requestedUserIds.contains(id))
                .map(relationMap::get)
                .toList();
        memberProjectRelationRepository.deleteAll(relationsToRemove);

        List<MemberProjectRelation> relationsToSave = request.members().stream()
                .map(memberRequest -> {
                    Long memberId = memberRequest.memberId();
                    if (relationMap.containsKey(memberId)) {
                        return relationMap.get(memberId).updateAll(
                                memberRequest.memberRole(),
                                memberRequest.memberDescription(),
                                memberRequest.isTeamMember()
                        );
                    } else {
                        return MemberProjectRelation.builder()
                                .projectId(projectId)
                                .userId(memberId)
                                .role(memberRequest.memberRole())
                                .description(memberRequest.memberDescription())
                                .isTeamMember(memberRequest.isTeamMember())
                                .build();
                    }
                })
                .toList();

        memberProjectRelationRepository.saveAll(relationsToSave);
    }

    private void updateProjectLinks(Long projectId, ProjectUpdateRequest request) {
        projectLinkRepository.deleteAllByProjectId(projectId);

        List<ProjectLink> linksToSave = request.links().stream()
                .map(linkRequest -> ProjectLink.builder()
                        .projectId(projectId)
                        .title(linkRequest.linkTitle())
                        .url(linkRequest.linkUrl())
                        .build()
                ).toList();

        projectLinkRepository.saveAll(linksToSave);
    }

    @Transactional
    public void deleteProject (Long writerId, Long projectId) {
        Project project = getProjectById(projectId);
        validateWriter(project, writerId);

        projectLinkRepository.deleteAllByProjectId(projectId);
        memberProjectRelationRepository.deleteAllByProjectId(projectId);
        projectRepository.delete(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectLinkDao> fetchAllLinks () {
        return projectQueryRepository.findAllLinks();
    }

    @Transactional(readOnly = true)
    public List<ProjectMemberVo> fetchById (Long id) {
        List<ProjectMemberDao> project = projectQueryRepository.findById(id);
        if (project.isEmpty()) throw new NotFoundDBEntityException("잘못된 프로젝트 조회입니다.");

        val projectMemberIds = project.stream()
                .filter(ProjectMemberDao::memberHasProfile)
                .map(ProjectMemberDao::memberId)
                .toList();

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
        return projectQueryRepository.findLinksById(id);
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

    private Project getProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundDBEntityException("잘못된 프로젝트 조회입니다."));
    }

    @Transactional(readOnly = true)
    public ProjectDetailResponse getProjectDetailResponseById(Long projectId) {
        Project project = getProjectById(projectId);
        List<MemberProjectRelation> projectUsers = memberProjectRelationRepository.findAllByProjectId(projectId);
        List<ProjectLink> projectLinks = projectLinkRepository.findAllByProjectId(projectId);

        // 추가로 필요한 user 정보 가져오기
        List<Long> userIds = projectUsers.stream().map(MemberProjectRelation::getUserId).toList();
        List<InternalUserDetails> projectUsersDetails = Objects.requireNonNull(platformClient.getInternalUserDetails(authConfig.getPlatformApiKey(),
                authConfig.getPlatformServiceName(), userIds).getBody()).getData();
        List<Member> hasProfileList = memberRepository.findAllByIdIn(userIds);

        List<ProjectDetailResponse.ProjectMemberResponse> memberResponse = projectResponseMapper.toListProjectMemberResponse(projectUsers, projectUsersDetails, hasProfileList);
        return projectResponseMapper.toProjectDetailResponse(project, memberResponse, projectLinks);
    }

    private void validateImageCount(int imageCount) {
        if (imageCount > 10) {
            throw new WrongImageInputException("이미지 개수를 초과했습니다.", "OutOfNumberImages");
        }
    }

    private void validateWriter(Project project, Long writerId) {
        if (!Objects.equals(project.getWriterId(), writerId)) {
            throw new ClientBadRequestException("수정 권한이 없는 유저입니다.");
        }
    }
}
