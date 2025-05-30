package org.sopt.makers.internal.project.mapper;

import org.mapstruct.Mapper;
import org.sopt.makers.internal.project.dto.response.ProjectMemberDao;
import org.sopt.makers.internal.project.dto.response.ProjectMemberVo;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    ProjectMemberVo projectMemberDaoToProjectMemberVo(ProjectMemberDao dao, List<Integer> memberGenerations);
}
