package org.sopt.makers.internal.project.mapper;

import lombok.val;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.project.domain.Project;
import org.sopt.makers.internal.dto.internal.InternalMemberProjectResponse;
import org.sopt.makers.internal.dto.internal.InternalProjectDetailResponse;
import org.sopt.makers.internal.dto.internal.InternalProjectResponse;
import org.sopt.makers.internal.project.dto.response.ProjectDetailResponse;
import org.sopt.makers.internal.project.dto.response.ProjectLinkDao;
import org.sopt.makers.internal.project.dto.response.ProjectMemberVo;
import org.sopt.makers.internal.project.dto.response.ProjectResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component
public class ProjectResponseMapper {

    public ProjectDetailResponse.ProjectMemberResponse toProjectDetailMemberResponse(ProjectMemberVo project) {
        return new ProjectDetailResponse.ProjectMemberResponse(
                project.memberId(), project.memberRole(), project.memberDesc(), project.isTeamMember(),
                project.memberName(), project.memberGenerations(), project.memberProfileImage(), project.memberHasProfile()
        );
    }

    public ProjectResponse.ProjectMemberResponse toProjectMemberResponse (ProjectMemberVo project) {
        return new ProjectResponse.ProjectMemberResponse(
                project.memberId(), project.memberName(), project.memberProfileImage()
        );
    }

    public ProjectResponse.ProjectLinkResponse toProjectLinkResponse (ProjectLinkDao project) {
        return new ProjectResponse.ProjectLinkResponse(project.linkId(), project.linkTitle(), project.linkUrl());
    }

    public ProjectDetailResponse.ProjectLinkResponse toProjectDetailLinkResponse (ProjectLinkDao project) {
        return new ProjectDetailResponse.ProjectLinkResponse(project.linkId(), project.linkTitle(), project.linkUrl());
    }
    public ProjectResponse toProjectResponse (Project project, List<ProjectMemberVo> projectMembers, List<ProjectLinkDao> projectLinks) {
        val linkResponses = projectLinks.stream().map(this::toProjectLinkResponse).collect(Collectors.toList());
        val memberResponses = projectMembers.stream().map(this::toProjectMemberResponse).collect(Collectors.toList());

        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getGeneration(),
                project.getCategory(),
                project.getServiceType(),
                project.getIsAvailable(),
                project.getIsFounding(),
                truncateString(project.getSummary()),
                truncateString(project.getDetail()),
                project.getLogoImage(),
                project.getThumbnailImage(),
                memberResponses,
                linkResponses
        );
    }

    public ProjectDetailResponse toProjectDetailResponse (List<ProjectMemberVo> projectMembers, List<ProjectLinkDao> projectLinks) {
        val projectInfo = projectMembers.get(0);
        val memberResponses = projectMembers.stream().map(this::toProjectDetailMemberResponse).collect(Collectors.toList());
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

    public InternalProjectDetailResponse toInternalProjectDetailResponse (List<ProjectMemberVo> projectMembers, List<ProjectLinkDao> projectLinks) {
        val projectInfo = projectMembers.get(0);
        val memberResponses = projectMembers.stream().map(this::toInternalProjectMemberResponse).collect(Collectors.toList());
        val linkResponses = projectLinks.stream().map(this::toInternalProjectDetailLinkResponse).collect(Collectors.toList());

        return new InternalProjectDetailResponse(
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

    public InternalProjectResponse toInternalProjectResponse (Project project, List<ProjectLinkDao> projectLinks) {
        val linkResponses = projectLinks.stream().map(this::toIntenralProjectLinkResponse).collect(Collectors.toList());

        return new InternalProjectResponse(
                project.getId(),
                project.getName(),
                project.getGeneration(),
                project.getCategory(),
                project.getServiceType(),
                project.getSummary(),
                project.getDetail(),
                project.getLogoImage(),
                project.getThumbnailImage(),
                project.getIsAvailable(),
                project.getIsFounding(),
                linkResponses
        );
    }

    public InternalProjectDetailResponse.ProjectMemberResponse toInternalProjectMemberResponse (ProjectMemberVo project) {
        return new InternalProjectDetailResponse.ProjectMemberResponse(
                project.memberId(), project.memberRole(), project.memberDesc(), project.isTeamMember(),
                project.memberName(), project.memberGenerations(), project.memberProfileImage(), project.memberHasProfile()
        );
    }

    public InternalProjectResponse.ProjectLinkResponse toIntenralProjectLinkResponse (ProjectLinkDao project) {
        return new InternalProjectResponse.ProjectLinkResponse(project.linkId(), project.linkTitle(), project.linkUrl());
    }

    public InternalProjectDetailResponse.ProjectLinkResponse toInternalProjectDetailLinkResponse (ProjectLinkDao project) {
        return new InternalProjectDetailResponse.ProjectLinkResponse(project.linkId(), project.linkTitle(), project.linkUrl());
    }

    public InternalMemberProjectResponse toInternalMemberProjectResponse (Member member, int count) {
        return new InternalMemberProjectResponse(member.getId(), member.getProfileImage(), count);
    }

    private String truncateString(String str) {
        if (str != null && str.length() > 20) {
            return str.substring(0, 20) + "...";
        }
        return str;
    }
}
