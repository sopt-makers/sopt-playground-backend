package org.sopt.makers.internal.mapper;

import lombok.val;
import org.sopt.makers.internal.domain.Project;
import org.sopt.makers.internal.dto.project.ProjectDetailResponse;
import org.sopt.makers.internal.dto.project.ProjectLinkDao;
import org.sopt.makers.internal.dto.project.ProjectMemberVo;
import org.sopt.makers.internal.dto.project.ProjectResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component
public class ProjectResponseMapper {

    public ProjectDetailResponse.ProjectMemberResponse toProjectMemberResponse (ProjectMemberVo project) {
        return new ProjectDetailResponse.ProjectMemberResponse(
                project.memberId(), project.memberRole(), project.memberDesc(), project.isTeamMember(),
                project.memberName(), project.memberGenerations(), project.memberProfileImage(), project.memberHasProfile()
        );
    }

    public ProjectResponse.ProjectLinkResponse toProjectLinkResponse (ProjectLinkDao project) {
        return new ProjectResponse.ProjectLinkResponse(project.linkId(), project.linkTitle(), project.linkUrl());
    }

    public ProjectDetailResponse.ProjectLinkResponse toProjectDetailLinkResponse (ProjectLinkDao project) {
        return new ProjectDetailResponse.ProjectLinkResponse(project.linkId(), project.linkTitle(), project.linkUrl());
    }
    public ProjectResponse toProjectResponse (Project project, List<ProjectLinkDao> projectLinks) {
        val linkResponses = projectLinks.stream().map(this::toProjectLinkResponse).collect(Collectors.toList());

        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getGeneration(),
                project.getCategory(),
                project.getServiceType(),
                project.getSummary(),
                project.getDetail(),
                project.getLogoImage(),
                project.getThumbnailImage(),
                linkResponses
        );
    }

    public ProjectDetailResponse toProjectDetailResponse (List<ProjectMemberVo> projectMembers, List<ProjectLinkDao> projectLinks) {
        val projectInfo = projectMembers.get(0);
        val memberResponses = projectMembers.stream().map(this::toProjectMemberResponse).collect(Collectors.toList());
        val linkResponses = projectLinks.stream().map(this::toProjectDetailLinkResponse).collect(Collectors.toList());

        return new ProjectDetailResponse(
                projectInfo.id(),
                projectInfo.name(),
                projectInfo.writerId(),
                projectInfo.generation(),
                projectInfo.category(),
                projectInfo.startAt(),
                projectInfo.endAt(),
                projectInfo.serviceType(),
                projectInfo.isAvailable(),
                projectInfo.isFounding(),
                projectInfo.summary(),
                projectInfo.detail(),
                projectInfo.logoImage(),
                projectInfo.thumbnailImage(),
                projectInfo.images(),
                projectInfo.createdAt(),
                projectInfo.updatedAt(),
                memberResponses,
                linkResponses
        );
    }
}
