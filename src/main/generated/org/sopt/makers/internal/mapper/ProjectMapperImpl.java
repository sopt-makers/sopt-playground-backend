package org.sopt.makers.internal.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.processing.Generated;
import org.sopt.makers.internal.dto.project.ProjectMemberDao;
import org.sopt.makers.internal.dto.project.ProjectMemberVo;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2023-05-01T17:08:53+0900",
    comments = "version: 1.5.2.Final, compiler: javac, environment: Java 17.0.3 (Amazon.com Inc.)"
)
@Component
public class ProjectMapperImpl implements ProjectMapper {

    @Override
    public ProjectMemberVo projectMemberDaoToProjectMemberVo(ProjectMemberDao dao, List<Integer> memberGenerations) {
        if ( dao == null && memberGenerations == null ) {
            return null;
        }

        Long id = null;
        String name = null;
        Long writerId = null;
        Integer generation = null;
        String category = null;
        LocalDate startAt = null;
        LocalDate endAt = null;
        String[] serviceType = null;
        Boolean isAvailable = null;
        Boolean isFounding = null;
        String summary = null;
        String detail = null;
        String logoImage = null;
        String thumbnailImage = null;
        String[] images = null;
        LocalDateTime createdAt = null;
        LocalDateTime updatedAt = null;
        Long memberId = null;
        String memberName = null;
        String memberProfileImage = null;
        Boolean memberHasProfile = null;
        String memberRole = null;
        String memberDesc = null;
        Boolean isTeamMember = null;
        if ( dao != null ) {
            id = dao.id();
            name = dao.name();
            writerId = dao.writerId();
            generation = dao.generation();
            category = dao.category();
            startAt = dao.startAt();
            endAt = dao.endAt();
            String[] serviceType1 = dao.serviceType();
            if ( serviceType1 != null ) {
                serviceType = Arrays.copyOf( serviceType1, serviceType1.length );
            }
            isAvailable = dao.isAvailable();
            isFounding = dao.isFounding();
            summary = dao.summary();
            detail = dao.detail();
            logoImage = dao.logoImage();
            thumbnailImage = dao.thumbnailImage();
            String[] images1 = dao.images();
            if ( images1 != null ) {
                images = Arrays.copyOf( images1, images1.length );
            }
            createdAt = dao.createdAt();
            updatedAt = dao.updatedAt();
            memberId = dao.memberId();
            memberName = dao.memberName();
            memberProfileImage = dao.memberProfileImage();
            memberHasProfile = dao.memberHasProfile();
            memberRole = dao.memberRole();
            memberDesc = dao.memberDesc();
            isTeamMember = dao.isTeamMember();
        }
        List<Integer> memberGenerations1 = null;
        List<Integer> list = memberGenerations;
        if ( list != null ) {
            memberGenerations1 = new ArrayList<Integer>( list );
        }

        ProjectMemberVo projectMemberVo = new ProjectMemberVo( id, name, writerId, generation, category, startAt, endAt, serviceType, isAvailable, isFounding, summary, detail, logoImage, thumbnailImage, images, createdAt, updatedAt, memberId, memberName, memberGenerations1, memberProfileImage, memberHasProfile, memberRole, memberDesc, isTeamMember );

        return projectMemberVo;
    }
}
