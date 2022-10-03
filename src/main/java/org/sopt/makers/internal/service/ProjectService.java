package org.sopt.makers.internal.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.domain.Link;
import org.sopt.makers.internal.domain.MemberProjectRelation;
import org.sopt.makers.internal.domain.Project;
import org.sopt.makers.internal.dto.ProjectDao;
import org.sopt.makers.internal.dto.ProjectSaveRequest;
import org.sopt.makers.internal.dto.ProjectUpdateRequest;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final LinkRepository linkRepository;
    private final MemberProjectRelationRepository relationRepository;
    private final ProjectQueryRepository projectQueryRepository;

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
                        .build()
        );

        relationRepository.saveAll(request.members().stream().map(memberRequest -> MemberProjectRelation.builder()
                .projectId(project.getId())
                .userId(memberRequest.memberId())
                .role(memberRequest.memberRole())
                .description(memberRequest.memberDesc())
                .isTeamMember(memberRequest.isTeamMember())
                .build()).collect(Collectors.toList()));

        linkRepository.saveAll(request.links().stream().map(linkRequest -> Link.builder()
                .projectId(project.getId())
                .title(linkRequest.linkTitle())
                .url(linkRequest.linkUrl())
                .build()).collect(Collectors.toList()));
    }

    @Transactional
    public void updateProject (Long id, ProjectUpdateRequest request) {
        val project = projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundDBEntityException("잘못된 프로젝트 조회입니다."));
        project.updateAll(
                request.name(), request.generation(), request.category(), request.startAt(),
                request.endAt(), request.isAvailable(), request.summary(), request.detail(),
                request.logoImage(), request.thumbnailImage(), request.images()
        );

        val relationList = relationRepository.findAllByProjectId(id);
        val relations = relationList.stream()
                .collect(Collectors.toMap(MemberProjectRelation::getUserId, Function.identity()));
        val relationUserSet = Set.copyOf(relationList.stream().map(MemberProjectRelation::getUserId).collect(Collectors.toList()));
        val requestedRelationUserSet = Set.copyOf(request.members().stream()
                .map(ProjectUpdateRequest.ProjectMemberUpdateRequest::memberId)
                .collect(Collectors.toList()));

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
                                memberRequest.memberDesc(),
                                memberRequest.isTeamMember()
                        );
            } else {
                return MemberProjectRelation.builder()
                        .projectId(project.getId())
                        .userId(memberRequest.memberId())
                        .role(memberRequest.memberRole())
                        .description(memberRequest.memberDesc())
                        .isTeamMember(memberRequest.isTeamMember())
                        .build();
            }
        }).collect(Collectors.toList()));

        linkRepository.saveAll(request.links().stream().map(linkRequest -> Link.builder()
                .projectId(project.getId())
                .title(linkRequest.linkTitle())
                .url(linkRequest.linkUrl())
                .build()).collect(Collectors.toList()));

    }

    @Transactional(readOnly = true)
    public List<ProjectDao> fetchAll () {
        return projectQueryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ProjectDao> fetchById (Long id) {
        val project = projectQueryRepository.findById(id);
        if (project.isEmpty()) throw new NotFoundDBEntityException("잘못된 프로젝트 조회입니다.");
        return project;
    }
}
