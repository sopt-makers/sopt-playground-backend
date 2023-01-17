package org.sopt.makers.internal.mapper;

import org.mapstruct.Mapper;
import org.sopt.makers.internal.dto.project.ProjectMemberDao;
import org.sopt.makers.internal.dto.project.ProjectMemberVo;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    ProjectMemberVo projectMemberDaoToProjectMemberVo(ProjectMemberDao dao, List<Integer> memberGenerations);
}
