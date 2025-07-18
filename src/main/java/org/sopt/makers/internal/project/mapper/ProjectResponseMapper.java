package org.sopt.makers.internal.project.mapper;

import java.util.List;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.SoptActivity;
import org.sopt.makers.internal.internal.dto.InternalMemberProjectResponse;
import org.sopt.makers.internal.internal.dto.InternalProjectResponse;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.project.domain.MemberProjectRelation;
import org.sopt.makers.internal.project.domain.Project;
import org.sopt.makers.internal.project.domain.ProjectLink;
import org.sopt.makers.internal.project.dto.dao.ProjectLinkDao;
import org.sopt.makers.internal.project.dto.response.allProject.ProjectMemberResponse;
import org.sopt.makers.internal.project.dto.response.allProject.ProjectResponse;
import org.sopt.makers.internal.project.dto.response.detailProject.ProjectDetailMemberResponse;
import org.sopt.makers.internal.project.dto.response.detailProject.ProjectDetailResponse;
import org.sopt.makers.internal.project.dto.response.detailProject.ProjectLinkResponse;
import org.springframework.stereotype.Component;


@Component
public class ProjectResponseMapper {

    public ProjectLinkResponse toProjectDetailLinkResponse (ProjectLink project) {
        return new ProjectLinkResponse(project.getId(), project.getTitle(), project.getUrl());
    }

    public ProjectResponse toProjectResponse(Project project, List<ProjectMemberResponse> projectMemberResponses) {
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
                projectMemberResponses
        );
    }

    public List<ProjectDetailMemberResponse> toListProjectMemberResponse(List<MemberProjectRelation> projectMembers,
                                                                                               List<InternalUserDetails> projectMembersDetails,
                                                                                               List<Member> hasProfileList) {
        return projectMembers.stream()
                .map(member -> {
                    InternalUserDetails details = projectMembersDetails.stream()
                            .filter(d -> d.userId().equals(member.getUserId()))
                            .findFirst().get();

                    Boolean memberHasProfile = hasProfileList.stream()
                            .filter(m -> m.getId().equals(member.getUserId()))
                            .findFirst().get().getHasProfile();

                    return new ProjectDetailMemberResponse(
                            member.getId(),
                            member.getRole(),
                            member.getDescription(),
                            member.getIsTeamMember(),
                            details.name(),
                            details.soptActivities().stream().map(SoptActivity::generation).toList(),
                            details.profileImage(),
                            memberHasProfile
                    );
                })
                .toList();
    }

    public ProjectDetailResponse toProjectDetailResponse(Project project,
                                                         List<ProjectDetailMemberResponse> memberResponses,
                                                         List<ProjectLink> projectLinks) {
        List<ProjectLinkResponse> linkResponses = projectLinks.stream()
                .map(this::toProjectDetailLinkResponse)
                .toList();

        return new ProjectDetailResponse(
                project.getId(),
                project.getName(),
                project.getWriterId(),
                project.getGeneration(),
                project.getCategory(),
                project.getStartAt(),
                project.getEndAt(),
                project.getServiceType(),
                project.getIsAvailable(),
                project.getIsFounding(),
                project.getSummary(),
                project.getDetail(),
                project.getLogoImage(),
                project.getThumbnailImage(),
                project.getImages(),
                project.getCreatedAt(),
                project.getUpdatedAt(),
                memberResponses,
                linkResponses
        );
    }

    public InternalProjectResponse toInternalProjectResponse (Project project, List<ProjectLinkDao> projectLinks) {
        List<ProjectLinkResponse> linkResponses = projectLinks.stream().map(this::toIntenralProjectLinkResponse).toList();

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

    public ProjectLinkResponse toIntenralProjectLinkResponse (ProjectLinkDao project) {
        return new ProjectLinkResponse(project.linkId(), project.linkTitle(), project.linkUrl());
    }

    public InternalMemberProjectResponse toInternalMemberProjectResponse (InternalUserDetails user, int count) {
        return new InternalMemberProjectResponse(user.userId(), user.profileImage(), count);
    }

    private String truncateString(String str) {
        if (str != null && str.length() > 20) {
            return str.substring(0, 20) + "...";
        }
        return str;
    }
}
