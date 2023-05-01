package org.sopt.makers.internal.mapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.processing.Generated;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.MemberCareer;
import org.sopt.makers.internal.domain.MemberLink;
import org.sopt.makers.internal.domain.MemberSoptActivity;
import org.sopt.makers.internal.domain.UserFavor;
import org.sopt.makers.internal.dto.internal.InternalMemberProfileResponse;
import org.sopt.makers.internal.dto.internal.InternalMemberProfileSpecificResponse;
import org.sopt.makers.internal.dto.internal.InternalMemberResponse;
import org.sopt.makers.internal.dto.member.ActivityVo;
import org.sopt.makers.internal.dto.member.MakersMemberProfileResponse;
import org.sopt.makers.internal.dto.member.MemberProfileProjectDao;
import org.sopt.makers.internal.dto.member.MemberProfileProjectVo;
import org.sopt.makers.internal.dto.member.MemberProfileResponse;
import org.sopt.makers.internal.dto.member.MemberProfileSpecificResponse;
import org.sopt.makers.internal.dto.member.MemberProjectVo;
import org.sopt.makers.internal.dto.member.MemberResponse;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2023-05-01T17:08:53+0900",
    comments = "version: 1.5.2.Final, compiler: javac, environment: Java 17.0.3 (Amazon.com Inc.)"
)
@Component
public class MemberMapperImpl implements MemberMapper {

    @Override
    public MemberResponse toResponse(Member member) {
        if ( member == null ) {
            return null;
        }

        Long id = null;
        String name = null;
        Integer generation = null;
        String profileImage = null;
        Boolean hasProfile = null;

        id = member.getId();
        name = member.getName();
        generation = member.getGeneration();
        profileImage = member.getProfileImage();
        hasProfile = member.getHasProfile();

        MemberResponse memberResponse = new MemberResponse( id, name, generation, profileImage, hasProfile );

        return memberResponse;
    }

    @Override
    public InternalMemberResponse toInternalResponse(Member member) {
        if ( member == null ) {
            return null;
        }

        Long id = null;
        String name = null;
        Integer generation = null;
        String profileImage = null;
        Boolean hasProfile = null;

        id = member.getId();
        name = member.getName();
        generation = member.getGeneration();
        profileImage = member.getProfileImage();
        hasProfile = member.getHasProfile();

        InternalMemberResponse internalMemberResponse = new InternalMemberResponse( id, name, generation, profileImage, hasProfile );

        return internalMemberResponse;
    }

    @Override
    public MemberProfileResponse toProfileResponse(Member member) {
        if ( member == null ) {
            return null;
        }

        Long id = null;
        String name = null;
        String profileImage = null;
        LocalDate birthday = null;
        String phone = null;
        String email = null;
        String address = null;
        String university = null;
        String major = null;
        String introduction = null;
        String skill = null;
        String mbti = null;
        String mbtiDescription = null;
        Double sojuCapacity = null;
        String interest = null;
        MemberProfileResponse.UserFavorResponse userFavor = null;
        String idealType = null;
        String selfIntroduction = null;
        List<MemberProfileResponse.MemberSoptActivityResponse> activities = null;
        List<MemberProfileResponse.MemberLinkResponse> links = null;
        List<MemberProfileResponse.MemberCareerResponse> careers = null;
        Boolean allowOfficial = null;

        id = member.getId();
        name = member.getName();
        profileImage = member.getProfileImage();
        birthday = member.getBirthday();
        phone = member.getPhone();
        email = member.getEmail();
        address = member.getAddress();
        university = member.getUniversity();
        major = member.getMajor();
        introduction = member.getIntroduction();
        skill = member.getSkill();
        mbti = member.getMbti();
        mbtiDescription = member.getMbtiDescription();
        sojuCapacity = member.getSojuCapacity();
        interest = member.getInterest();
        userFavor = userFavorToUserFavorResponse( member.getUserFavor() );
        idealType = member.getIdealType();
        selfIntroduction = member.getSelfIntroduction();
        activities = memberSoptActivityListToMemberSoptActivityResponseList( member.getActivities() );
        links = memberLinkListToMemberLinkResponseList( member.getLinks() );
        careers = memberCareerListToMemberCareerResponseList( member.getCareers() );
        allowOfficial = member.getAllowOfficial();

        MemberProfileResponse memberProfileResponse = new MemberProfileResponse( id, name, profileImage, birthday, phone, email, address, university, major, introduction, skill, mbti, mbtiDescription, sojuCapacity, interest, userFavor, idealType, selfIntroduction, activities, links, careers, allowOfficial );

        return memberProfileResponse;
    }

    @Override
    public InternalMemberProfileResponse toInternalProfileResponse(Member member) {
        if ( member == null ) {
            return null;
        }

        Long id = null;
        String name = null;
        String profileImage = null;
        LocalDate birthday = null;
        String phone = null;
        String email = null;
        String address = null;
        String university = null;
        String major = null;
        String introduction = null;
        String skill = null;
        String mbti = null;
        String mbtiDescription = null;
        Double sojuCapacity = null;
        String interest = null;
        InternalMemberProfileResponse.UserFavorResponse userFavor = null;
        String idealType = null;
        String selfIntroduction = null;
        List<InternalMemberProfileResponse.MemberSoptActivityResponse> activities = null;
        List<InternalMemberProfileResponse.MemberLinkResponse> links = null;
        List<InternalMemberProfileResponse.MemberCareerResponse> careers = null;
        Boolean allowOfficial = null;

        id = member.getId();
        name = member.getName();
        profileImage = member.getProfileImage();
        birthday = member.getBirthday();
        phone = member.getPhone();
        email = member.getEmail();
        address = member.getAddress();
        university = member.getUniversity();
        major = member.getMajor();
        introduction = member.getIntroduction();
        skill = member.getSkill();
        mbti = member.getMbti();
        mbtiDescription = member.getMbtiDescription();
        sojuCapacity = member.getSojuCapacity();
        interest = member.getInterest();
        userFavor = userFavorToUserFavorResponse1( member.getUserFavor() );
        idealType = member.getIdealType();
        selfIntroduction = member.getSelfIntroduction();
        activities = memberSoptActivityListToMemberSoptActivityResponseList1( member.getActivities() );
        links = memberLinkListToMemberLinkResponseList1( member.getLinks() );
        careers = memberCareerListToMemberCareerResponseList1( member.getCareers() );
        allowOfficial = member.getAllowOfficial();

        InternalMemberProfileResponse internalMemberProfileResponse = new InternalMemberProfileResponse( id, name, profileImage, birthday, phone, email, address, university, major, introduction, skill, mbti, mbtiDescription, sojuCapacity, interest, userFavor, idealType, selfIntroduction, activities, links, careers, allowOfficial );

        return internalMemberProfileResponse;
    }

    @Override
    public MakersMemberProfileResponse toMakersMemberProfileResponse(Member member) {
        if ( member == null ) {
            return null;
        }

        Long id = null;
        String name = null;
        String profileImage = null;
        List<MakersMemberProfileResponse.MemberSoptActivityResponse> activities = null;
        List<MakersMemberProfileResponse.MemberCareerResponse> careers = null;

        id = member.getId();
        name = member.getName();
        profileImage = member.getProfileImage();
        activities = memberSoptActivityListToMemberSoptActivityResponseList2( member.getActivities() );
        careers = memberCareerListToMemberCareerResponseList2( member.getCareers() );

        MakersMemberProfileResponse makersMemberProfileResponse = new MakersMemberProfileResponse( id, name, profileImage, activities, careers );

        return makersMemberProfileResponse;
    }

    @Override
    public MemberProfileProjectVo toSoptMemberProfileProjectVo(MemberSoptActivity member, List<MemberProjectVo> projects) {
        if ( member == null && projects == null ) {
            return null;
        }

        Long id = null;
        Integer generation = null;
        String part = null;
        String team = null;
        if ( member != null ) {
            id = member.getId();
            generation = member.getGeneration();
            part = member.getPart();
            team = member.getTeam();
        }
        List<MemberProjectVo> projects1 = null;
        List<MemberProjectVo> list = projects;
        if ( list != null ) {
            projects1 = new ArrayList<MemberProjectVo>( list );
        }

        MemberProfileProjectVo memberProfileProjectVo = new MemberProfileProjectVo( id, generation, part, team, projects1 );

        return memberProfileProjectVo;
    }

    @Override
    public MemberProfileSpecificResponse toProfileSpecificResponse(Member member, boolean isMine, List<MemberProfileProjectDao> projects, List<MemberProfileSpecificResponse.MemberActivityResponse> activities, List<MemberProfileProjectVo> soptActivities) {
        if ( member == null && projects == null && activities == null && soptActivities == null ) {
            return null;
        }

        String name = null;
        String profileImage = null;
        LocalDate birthday = null;
        String phone = null;
        String email = null;
        String address = null;
        String university = null;
        String major = null;
        String introduction = null;
        String skill = null;
        String mbti = null;
        String mbtiDescription = null;
        Double sojuCapacity = null;
        String interest = null;
        MemberProfileSpecificResponse.UserFavorResponse userFavor = null;
        String idealType = null;
        String selfIntroduction = null;
        List<MemberProfileSpecificResponse.MemberLinkResponse> links = null;
        List<MemberProfileSpecificResponse.MemberCareerResponse> careers = null;
        Boolean allowOfficial = null;
        if ( member != null ) {
            name = member.getName();
            profileImage = member.getProfileImage();
            birthday = member.getBirthday();
            phone = member.getPhone();
            email = member.getEmail();
            address = member.getAddress();
            university = member.getUniversity();
            major = member.getMajor();
            introduction = member.getIntroduction();
            skill = member.getSkill();
            mbti = member.getMbti();
            mbtiDescription = member.getMbtiDescription();
            sojuCapacity = member.getSojuCapacity();
            interest = member.getInterest();
            userFavor = userFavorToUserFavorResponse2( member.getUserFavor() );
            idealType = member.getIdealType();
            selfIntroduction = member.getSelfIntroduction();
            links = memberLinkListToMemberLinkResponseList2( member.getLinks() );
            careers = memberCareerListToMemberCareerResponseList3( member.getCareers() );
            allowOfficial = member.getAllowOfficial();
        }
        Boolean isMine1 = null;
        isMine1 = isMine;
        List<MemberProfileSpecificResponse.MemberProjectResponse> projects1 = null;
        projects1 = memberProfileProjectDaoListToMemberProjectResponseList( projects );
        List<MemberProfileSpecificResponse.MemberActivityResponse> activities1 = null;
        List<MemberProfileSpecificResponse.MemberActivityResponse> list = activities;
        if ( list != null ) {
            activities1 = new ArrayList<MemberProfileSpecificResponse.MemberActivityResponse>( list );
        }
        List<MemberProfileSpecificResponse.SoptMemberActivityResponse> soptActivities1 = null;
        soptActivities1 = memberProfileProjectVoListToSoptMemberActivityResponseList( soptActivities );

        MemberProfileSpecificResponse memberProfileSpecificResponse = new MemberProfileSpecificResponse( name, profileImage, birthday, phone, email, address, university, major, introduction, skill, mbti, mbtiDescription, sojuCapacity, interest, userFavor, idealType, selfIntroduction, activities1, soptActivities1, links, projects1, careers, allowOfficial, isMine1 );

        return memberProfileSpecificResponse;
    }

    @Override
    public MemberProfileSpecificResponse toProfileSpecificResponse(Member member, boolean isMine, List<MemberProfileProjectDao> projects, List<MemberProfileSpecificResponse.MemberActivityResponse> activities) {
        if ( member == null && projects == null && activities == null ) {
            return null;
        }

        String name = null;
        String profileImage = null;
        LocalDate birthday = null;
        String phone = null;
        String email = null;
        String address = null;
        String university = null;
        String major = null;
        String introduction = null;
        String skill = null;
        String mbti = null;
        String mbtiDescription = null;
        Double sojuCapacity = null;
        String interest = null;
        MemberProfileSpecificResponse.UserFavorResponse userFavor = null;
        String idealType = null;
        String selfIntroduction = null;
        List<MemberProfileSpecificResponse.MemberLinkResponse> links = null;
        List<MemberProfileSpecificResponse.MemberCareerResponse> careers = null;
        Boolean allowOfficial = null;
        if ( member != null ) {
            name = member.getName();
            profileImage = member.getProfileImage();
            birthday = member.getBirthday();
            phone = member.getPhone();
            email = member.getEmail();
            address = member.getAddress();
            university = member.getUniversity();
            major = member.getMajor();
            introduction = member.getIntroduction();
            skill = member.getSkill();
            mbti = member.getMbti();
            mbtiDescription = member.getMbtiDescription();
            sojuCapacity = member.getSojuCapacity();
            interest = member.getInterest();
            userFavor = userFavorToUserFavorResponse2( member.getUserFavor() );
            idealType = member.getIdealType();
            selfIntroduction = member.getSelfIntroduction();
            links = memberLinkListToMemberLinkResponseList2( member.getLinks() );
            careers = memberCareerListToMemberCareerResponseList3( member.getCareers() );
            allowOfficial = member.getAllowOfficial();
        }
        Boolean isMine1 = null;
        isMine1 = isMine;
        List<MemberProfileSpecificResponse.MemberProjectResponse> projects1 = null;
        projects1 = memberProfileProjectDaoListToMemberProjectResponseList( projects );
        List<MemberProfileSpecificResponse.MemberActivityResponse> activities1 = null;
        List<MemberProfileSpecificResponse.MemberActivityResponse> list = activities;
        if ( list != null ) {
            activities1 = new ArrayList<MemberProfileSpecificResponse.MemberActivityResponse>( list );
        }

        List<MemberProfileSpecificResponse.SoptMemberActivityResponse> soptActivities = null;

        MemberProfileSpecificResponse memberProfileSpecificResponse = new MemberProfileSpecificResponse( name, profileImage, birthday, phone, email, address, university, major, introduction, skill, mbti, mbtiDescription, sojuCapacity, interest, userFavor, idealType, selfIntroduction, activities1, soptActivities, links, projects1, careers, allowOfficial, isMine1 );

        return memberProfileSpecificResponse;
    }

    @Override
    public InternalMemberProfileSpecificResponse toInternalProfileSpecificResponse(Member member, boolean isMine, List<MemberProfileProjectDao> projects, List<InternalMemberProfileSpecificResponse.MemberActivityResponse> activities) {
        if ( member == null && projects == null && activities == null ) {
            return null;
        }

        String name = null;
        String profileImage = null;
        LocalDate birthday = null;
        String phone = null;
        String email = null;
        String address = null;
        String university = null;
        String major = null;
        String introduction = null;
        String skill = null;
        String interest = null;
        List<InternalMemberProfileSpecificResponse.MemberLinkResponse> links = null;
        List<InternalMemberProfileSpecificResponse.MemberCareerResponse> careers = null;
        Boolean allowOfficial = null;
        if ( member != null ) {
            name = member.getName();
            profileImage = member.getProfileImage();
            birthday = member.getBirthday();
            phone = member.getPhone();
            email = member.getEmail();
            address = member.getAddress();
            university = member.getUniversity();
            major = member.getMajor();
            introduction = member.getIntroduction();
            skill = member.getSkill();
            interest = member.getInterest();
            links = memberLinkListToMemberLinkResponseList3( member.getLinks() );
            careers = memberCareerListToMemberCareerResponseList4( member.getCareers() );
            allowOfficial = member.getAllowOfficial();
        }
        Boolean isMine1 = null;
        isMine1 = isMine;
        List<InternalMemberProfileSpecificResponse.MemberProjectResponse> projects1 = null;
        projects1 = memberProfileProjectDaoListToMemberProjectResponseList1( projects );
        List<InternalMemberProfileSpecificResponse.MemberActivityResponse> activities1 = null;
        List<InternalMemberProfileSpecificResponse.MemberActivityResponse> list = activities;
        if ( list != null ) {
            activities1 = new ArrayList<InternalMemberProfileSpecificResponse.MemberActivityResponse>( list );
        }

        InternalMemberProfileSpecificResponse internalMemberProfileSpecificResponse = new InternalMemberProfileSpecificResponse( name, profileImage, birthday, phone, email, address, university, major, introduction, skill, interest, activities1, links, projects1, careers, allowOfficial, isMine1 );

        return internalMemberProfileSpecificResponse;
    }

    @Override
    public ActivityVo toActivityInfoVo(MemberSoptActivity activity, boolean isProject) {
        if ( activity == null ) {
            return null;
        }

        Long id = null;
        Integer generation = null;
        String team = null;
        String part = null;
        if ( activity != null ) {
            id = activity.getId();
            generation = activity.getGeneration();
            team = activity.getTeam();
            part = activity.getPart();
        }
        boolean isProject1 = false;
        isProject1 = isProject;

        ActivityVo activityVo = new ActivityVo( id, generation, team, part, isProject1 );

        return activityVo;
    }

    @Override
    public ActivityVo toActivityInfoVo(MemberProfileProjectDao project, boolean isProject, String part) {
        if ( project == null && part == null ) {
            return null;
        }

        String team = null;
        Long id = null;
        Integer generation = null;
        if ( project != null ) {
            team = project.name();
            id = project.id();
            generation = project.generation();
        }
        boolean isProject1 = false;
        isProject1 = isProject;
        String part1 = null;
        part1 = part;

        ActivityVo activityVo = new ActivityVo( id, generation, team, part1, isProject1 );

        return activityVo;
    }

    @Override
    public MemberProjectVo toActivityInfoVo(MemberProfileProjectDao project) {
        if ( project == null ) {
            return null;
        }

        Long id = null;
        Integer generation = null;
        String name = null;
        String category = null;

        id = project.id();
        generation = project.generation();
        name = project.name();
        category = project.category();

        MemberProjectVo memberProjectVo = new MemberProjectVo( id, generation, name, category );

        return memberProjectVo;
    }

    protected MemberProfileResponse.UserFavorResponse userFavorToUserFavorResponse(UserFavor userFavor) {
        if ( userFavor == null ) {
            return null;
        }

        Boolean isPourSauceLover = null;
        Boolean isHardPeachLover = null;
        Boolean isMintChocoLover = null;
        Boolean isRedBeanFishBreadLover = null;
        Boolean isSojuLover = null;
        Boolean isRiceTteokLover = null;

        isPourSauceLover = userFavor.getIsPourSauceLover();
        isHardPeachLover = userFavor.getIsHardPeachLover();
        isMintChocoLover = userFavor.getIsMintChocoLover();
        isRedBeanFishBreadLover = userFavor.getIsRedBeanFishBreadLover();
        isSojuLover = userFavor.getIsSojuLover();
        isRiceTteokLover = userFavor.getIsRiceTteokLover();

        MemberProfileResponse.UserFavorResponse userFavorResponse = new MemberProfileResponse.UserFavorResponse( isPourSauceLover, isHardPeachLover, isMintChocoLover, isRedBeanFishBreadLover, isSojuLover, isRiceTteokLover );

        return userFavorResponse;
    }

    protected MemberProfileResponse.MemberSoptActivityResponse memberSoptActivityToMemberSoptActivityResponse(MemberSoptActivity memberSoptActivity) {
        if ( memberSoptActivity == null ) {
            return null;
        }

        Long id = null;
        Integer generation = null;
        String part = null;
        String team = null;

        id = memberSoptActivity.getId();
        generation = memberSoptActivity.getGeneration();
        part = memberSoptActivity.getPart();
        team = memberSoptActivity.getTeam();

        MemberProfileResponse.MemberSoptActivityResponse memberSoptActivityResponse = new MemberProfileResponse.MemberSoptActivityResponse( id, generation, part, team );

        return memberSoptActivityResponse;
    }

    protected List<MemberProfileResponse.MemberSoptActivityResponse> memberSoptActivityListToMemberSoptActivityResponseList(List<MemberSoptActivity> list) {
        if ( list == null ) {
            return null;
        }

        List<MemberProfileResponse.MemberSoptActivityResponse> list1 = new ArrayList<MemberProfileResponse.MemberSoptActivityResponse>( list.size() );
        for ( MemberSoptActivity memberSoptActivity : list ) {
            list1.add( memberSoptActivityToMemberSoptActivityResponse( memberSoptActivity ) );
        }

        return list1;
    }

    protected MemberProfileResponse.MemberLinkResponse memberLinkToMemberLinkResponse(MemberLink memberLink) {
        if ( memberLink == null ) {
            return null;
        }

        Long id = null;
        String title = null;
        String url = null;

        id = memberLink.getId();
        title = memberLink.getTitle();
        url = memberLink.getUrl();

        MemberProfileResponse.MemberLinkResponse memberLinkResponse = new MemberProfileResponse.MemberLinkResponse( id, title, url );

        return memberLinkResponse;
    }

    protected List<MemberProfileResponse.MemberLinkResponse> memberLinkListToMemberLinkResponseList(List<MemberLink> list) {
        if ( list == null ) {
            return null;
        }

        List<MemberProfileResponse.MemberLinkResponse> list1 = new ArrayList<MemberProfileResponse.MemberLinkResponse>( list.size() );
        for ( MemberLink memberLink : list ) {
            list1.add( memberLinkToMemberLinkResponse( memberLink ) );
        }

        return list1;
    }

    protected MemberProfileResponse.MemberCareerResponse memberCareerToMemberCareerResponse(MemberCareer memberCareer) {
        if ( memberCareer == null ) {
            return null;
        }

        Long id = null;
        String companyName = null;
        String title = null;
        String startDate = null;
        String endDate = null;
        Boolean isCurrent = null;

        id = memberCareer.getId();
        companyName = memberCareer.getCompanyName();
        title = memberCareer.getTitle();
        startDate = memberCareer.getStartDate();
        endDate = memberCareer.getEndDate();
        isCurrent = memberCareer.getIsCurrent();

        MemberProfileResponse.MemberCareerResponse memberCareerResponse = new MemberProfileResponse.MemberCareerResponse( id, companyName, title, startDate, endDate, isCurrent );

        return memberCareerResponse;
    }

    protected List<MemberProfileResponse.MemberCareerResponse> memberCareerListToMemberCareerResponseList(List<MemberCareer> list) {
        if ( list == null ) {
            return null;
        }

        List<MemberProfileResponse.MemberCareerResponse> list1 = new ArrayList<MemberProfileResponse.MemberCareerResponse>( list.size() );
        for ( MemberCareer memberCareer : list ) {
            list1.add( memberCareerToMemberCareerResponse( memberCareer ) );
        }

        return list1;
    }

    protected InternalMemberProfileResponse.UserFavorResponse userFavorToUserFavorResponse1(UserFavor userFavor) {
        if ( userFavor == null ) {
            return null;
        }

        Boolean isPourSauceLover = null;
        Boolean isHardPeachLover = null;
        Boolean isMintChocoLover = null;
        Boolean isRedBeanFishBreadLover = null;
        Boolean isSojuLover = null;
        Boolean isRiceTteokLover = null;

        isPourSauceLover = userFavor.getIsPourSauceLover();
        isHardPeachLover = userFavor.getIsHardPeachLover();
        isMintChocoLover = userFavor.getIsMintChocoLover();
        isRedBeanFishBreadLover = userFavor.getIsRedBeanFishBreadLover();
        isSojuLover = userFavor.getIsSojuLover();
        isRiceTteokLover = userFavor.getIsRiceTteokLover();

        InternalMemberProfileResponse.UserFavorResponse userFavorResponse = new InternalMemberProfileResponse.UserFavorResponse( isPourSauceLover, isHardPeachLover, isMintChocoLover, isRedBeanFishBreadLover, isSojuLover, isRiceTteokLover );

        return userFavorResponse;
    }

    protected InternalMemberProfileResponse.MemberSoptActivityResponse memberSoptActivityToMemberSoptActivityResponse1(MemberSoptActivity memberSoptActivity) {
        if ( memberSoptActivity == null ) {
            return null;
        }

        Long id = null;
        Integer generation = null;
        String part = null;
        String team = null;

        id = memberSoptActivity.getId();
        generation = memberSoptActivity.getGeneration();
        part = memberSoptActivity.getPart();
        team = memberSoptActivity.getTeam();

        InternalMemberProfileResponse.MemberSoptActivityResponse memberSoptActivityResponse = new InternalMemberProfileResponse.MemberSoptActivityResponse( id, generation, part, team );

        return memberSoptActivityResponse;
    }

    protected List<InternalMemberProfileResponse.MemberSoptActivityResponse> memberSoptActivityListToMemberSoptActivityResponseList1(List<MemberSoptActivity> list) {
        if ( list == null ) {
            return null;
        }

        List<InternalMemberProfileResponse.MemberSoptActivityResponse> list1 = new ArrayList<InternalMemberProfileResponse.MemberSoptActivityResponse>( list.size() );
        for ( MemberSoptActivity memberSoptActivity : list ) {
            list1.add( memberSoptActivityToMemberSoptActivityResponse1( memberSoptActivity ) );
        }

        return list1;
    }

    protected InternalMemberProfileResponse.MemberLinkResponse memberLinkToMemberLinkResponse1(MemberLink memberLink) {
        if ( memberLink == null ) {
            return null;
        }

        Long id = null;
        String title = null;
        String url = null;

        id = memberLink.getId();
        title = memberLink.getTitle();
        url = memberLink.getUrl();

        InternalMemberProfileResponse.MemberLinkResponse memberLinkResponse = new InternalMemberProfileResponse.MemberLinkResponse( id, title, url );

        return memberLinkResponse;
    }

    protected List<InternalMemberProfileResponse.MemberLinkResponse> memberLinkListToMemberLinkResponseList1(List<MemberLink> list) {
        if ( list == null ) {
            return null;
        }

        List<InternalMemberProfileResponse.MemberLinkResponse> list1 = new ArrayList<InternalMemberProfileResponse.MemberLinkResponse>( list.size() );
        for ( MemberLink memberLink : list ) {
            list1.add( memberLinkToMemberLinkResponse1( memberLink ) );
        }

        return list1;
    }

    protected InternalMemberProfileResponse.MemberCareerResponse memberCareerToMemberCareerResponse1(MemberCareer memberCareer) {
        if ( memberCareer == null ) {
            return null;
        }

        Long id = null;
        String companyName = null;
        String title = null;
        String startDate = null;
        String endDate = null;
        Boolean isCurrent = null;

        id = memberCareer.getId();
        companyName = memberCareer.getCompanyName();
        title = memberCareer.getTitle();
        startDate = memberCareer.getStartDate();
        endDate = memberCareer.getEndDate();
        isCurrent = memberCareer.getIsCurrent();

        InternalMemberProfileResponse.MemberCareerResponse memberCareerResponse = new InternalMemberProfileResponse.MemberCareerResponse( id, companyName, title, startDate, endDate, isCurrent );

        return memberCareerResponse;
    }

    protected List<InternalMemberProfileResponse.MemberCareerResponse> memberCareerListToMemberCareerResponseList1(List<MemberCareer> list) {
        if ( list == null ) {
            return null;
        }

        List<InternalMemberProfileResponse.MemberCareerResponse> list1 = new ArrayList<InternalMemberProfileResponse.MemberCareerResponse>( list.size() );
        for ( MemberCareer memberCareer : list ) {
            list1.add( memberCareerToMemberCareerResponse1( memberCareer ) );
        }

        return list1;
    }

    protected MakersMemberProfileResponse.MemberSoptActivityResponse memberSoptActivityToMemberSoptActivityResponse2(MemberSoptActivity memberSoptActivity) {
        if ( memberSoptActivity == null ) {
            return null;
        }

        Long id = null;
        Integer generation = null;

        id = memberSoptActivity.getId();
        generation = memberSoptActivity.getGeneration();

        MakersMemberProfileResponse.MemberSoptActivityResponse memberSoptActivityResponse = new MakersMemberProfileResponse.MemberSoptActivityResponse( id, generation );

        return memberSoptActivityResponse;
    }

    protected List<MakersMemberProfileResponse.MemberSoptActivityResponse> memberSoptActivityListToMemberSoptActivityResponseList2(List<MemberSoptActivity> list) {
        if ( list == null ) {
            return null;
        }

        List<MakersMemberProfileResponse.MemberSoptActivityResponse> list1 = new ArrayList<MakersMemberProfileResponse.MemberSoptActivityResponse>( list.size() );
        for ( MemberSoptActivity memberSoptActivity : list ) {
            list1.add( memberSoptActivityToMemberSoptActivityResponse2( memberSoptActivity ) );
        }

        return list1;
    }

    protected MakersMemberProfileResponse.MemberCareerResponse memberCareerToMemberCareerResponse2(MemberCareer memberCareer) {
        if ( memberCareer == null ) {
            return null;
        }

        Long id = null;
        String companyName = null;
        String title = null;
        Boolean isCurrent = null;

        id = memberCareer.getId();
        companyName = memberCareer.getCompanyName();
        title = memberCareer.getTitle();
        isCurrent = memberCareer.getIsCurrent();

        MakersMemberProfileResponse.MemberCareerResponse memberCareerResponse = new MakersMemberProfileResponse.MemberCareerResponse( id, companyName, title, isCurrent );

        return memberCareerResponse;
    }

    protected List<MakersMemberProfileResponse.MemberCareerResponse> memberCareerListToMemberCareerResponseList2(List<MemberCareer> list) {
        if ( list == null ) {
            return null;
        }

        List<MakersMemberProfileResponse.MemberCareerResponse> list1 = new ArrayList<MakersMemberProfileResponse.MemberCareerResponse>( list.size() );
        for ( MemberCareer memberCareer : list ) {
            list1.add( memberCareerToMemberCareerResponse2( memberCareer ) );
        }

        return list1;
    }

    protected MemberProfileSpecificResponse.UserFavorResponse userFavorToUserFavorResponse2(UserFavor userFavor) {
        if ( userFavor == null ) {
            return null;
        }

        Boolean isPourSauceLover = null;
        Boolean isHardPeachLover = null;
        Boolean isMintChocoLover = null;
        Boolean isRedBeanFishBreadLover = null;
        Boolean isSojuLover = null;
        Boolean isRiceTteokLover = null;

        isPourSauceLover = userFavor.getIsPourSauceLover();
        isHardPeachLover = userFavor.getIsHardPeachLover();
        isMintChocoLover = userFavor.getIsMintChocoLover();
        isRedBeanFishBreadLover = userFavor.getIsRedBeanFishBreadLover();
        isSojuLover = userFavor.getIsSojuLover();
        isRiceTteokLover = userFavor.getIsRiceTteokLover();

        MemberProfileSpecificResponse.UserFavorResponse userFavorResponse = new MemberProfileSpecificResponse.UserFavorResponse( isPourSauceLover, isHardPeachLover, isMintChocoLover, isRedBeanFishBreadLover, isSojuLover, isRiceTteokLover );

        return userFavorResponse;
    }

    protected MemberProfileSpecificResponse.MemberLinkResponse memberLinkToMemberLinkResponse2(MemberLink memberLink) {
        if ( memberLink == null ) {
            return null;
        }

        Long id = null;
        String title = null;
        String url = null;

        id = memberLink.getId();
        title = memberLink.getTitle();
        url = memberLink.getUrl();

        MemberProfileSpecificResponse.MemberLinkResponse memberLinkResponse = new MemberProfileSpecificResponse.MemberLinkResponse( id, title, url );

        return memberLinkResponse;
    }

    protected List<MemberProfileSpecificResponse.MemberLinkResponse> memberLinkListToMemberLinkResponseList2(List<MemberLink> list) {
        if ( list == null ) {
            return null;
        }

        List<MemberProfileSpecificResponse.MemberLinkResponse> list1 = new ArrayList<MemberProfileSpecificResponse.MemberLinkResponse>( list.size() );
        for ( MemberLink memberLink : list ) {
            list1.add( memberLinkToMemberLinkResponse2( memberLink ) );
        }

        return list1;
    }

    protected MemberProfileSpecificResponse.MemberCareerResponse memberCareerToMemberCareerResponse3(MemberCareer memberCareer) {
        if ( memberCareer == null ) {
            return null;
        }

        Long id = null;
        String companyName = null;
        String title = null;
        String startDate = null;
        String endDate = null;
        Boolean isCurrent = null;

        id = memberCareer.getId();
        companyName = memberCareer.getCompanyName();
        title = memberCareer.getTitle();
        startDate = memberCareer.getStartDate();
        endDate = memberCareer.getEndDate();
        isCurrent = memberCareer.getIsCurrent();

        MemberProfileSpecificResponse.MemberCareerResponse memberCareerResponse = new MemberProfileSpecificResponse.MemberCareerResponse( id, companyName, title, startDate, endDate, isCurrent );

        return memberCareerResponse;
    }

    protected List<MemberProfileSpecificResponse.MemberCareerResponse> memberCareerListToMemberCareerResponseList3(List<MemberCareer> list) {
        if ( list == null ) {
            return null;
        }

        List<MemberProfileSpecificResponse.MemberCareerResponse> list1 = new ArrayList<MemberProfileSpecificResponse.MemberCareerResponse>( list.size() );
        for ( MemberCareer memberCareer : list ) {
            list1.add( memberCareerToMemberCareerResponse3( memberCareer ) );
        }

        return list1;
    }

    protected MemberProfileSpecificResponse.SoptMemberActivityResponse memberProfileProjectVoToSoptMemberActivityResponse(MemberProfileProjectVo memberProfileProjectVo) {
        if ( memberProfileProjectVo == null ) {
            return null;
        }

        Integer generation = null;
        String part = null;
        String team = null;
        List<MemberProjectVo> projects = null;

        generation = memberProfileProjectVo.generation();
        part = memberProfileProjectVo.part();
        team = memberProfileProjectVo.team();
        List<MemberProjectVo> list = memberProfileProjectVo.projects();
        if ( list != null ) {
            projects = new ArrayList<MemberProjectVo>( list );
        }

        MemberProfileSpecificResponse.SoptMemberActivityResponse soptMemberActivityResponse = new MemberProfileSpecificResponse.SoptMemberActivityResponse( generation, part, team, projects );

        return soptMemberActivityResponse;
    }

    protected List<MemberProfileSpecificResponse.SoptMemberActivityResponse> memberProfileProjectVoListToSoptMemberActivityResponseList(List<MemberProfileProjectVo> list) {
        if ( list == null ) {
            return null;
        }

        List<MemberProfileSpecificResponse.SoptMemberActivityResponse> list1 = new ArrayList<MemberProfileSpecificResponse.SoptMemberActivityResponse>( list.size() );
        for ( MemberProfileProjectVo memberProfileProjectVo : list ) {
            list1.add( memberProfileProjectVoToSoptMemberActivityResponse( memberProfileProjectVo ) );
        }

        return list1;
    }

    protected MemberProfileSpecificResponse.MemberProjectResponse memberProfileProjectDaoToMemberProjectResponse(MemberProfileProjectDao memberProfileProjectDao) {
        if ( memberProfileProjectDao == null ) {
            return null;
        }

        Long id = null;
        String name = null;
        String summary = null;
        Integer generation = null;
        String category = null;
        String logoImage = null;
        String thumbnailImage = null;
        String[] serviceType = null;

        id = memberProfileProjectDao.id();
        name = memberProfileProjectDao.name();
        summary = memberProfileProjectDao.summary();
        generation = memberProfileProjectDao.generation();
        category = memberProfileProjectDao.category();
        logoImage = memberProfileProjectDao.logoImage();
        thumbnailImage = memberProfileProjectDao.thumbnailImage();
        String[] serviceType1 = memberProfileProjectDao.serviceType();
        if ( serviceType1 != null ) {
            serviceType = Arrays.copyOf( serviceType1, serviceType1.length );
        }

        MemberProfileSpecificResponse.MemberProjectResponse memberProjectResponse = new MemberProfileSpecificResponse.MemberProjectResponse( id, name, summary, generation, category, logoImage, thumbnailImage, serviceType );

        return memberProjectResponse;
    }

    protected List<MemberProfileSpecificResponse.MemberProjectResponse> memberProfileProjectDaoListToMemberProjectResponseList(List<MemberProfileProjectDao> list) {
        if ( list == null ) {
            return null;
        }

        List<MemberProfileSpecificResponse.MemberProjectResponse> list1 = new ArrayList<MemberProfileSpecificResponse.MemberProjectResponse>( list.size() );
        for ( MemberProfileProjectDao memberProfileProjectDao : list ) {
            list1.add( memberProfileProjectDaoToMemberProjectResponse( memberProfileProjectDao ) );
        }

        return list1;
    }

    protected InternalMemberProfileSpecificResponse.MemberLinkResponse memberLinkToMemberLinkResponse3(MemberLink memberLink) {
        if ( memberLink == null ) {
            return null;
        }

        Long id = null;
        String title = null;
        String url = null;

        id = memberLink.getId();
        title = memberLink.getTitle();
        url = memberLink.getUrl();

        InternalMemberProfileSpecificResponse.MemberLinkResponse memberLinkResponse = new InternalMemberProfileSpecificResponse.MemberLinkResponse( id, title, url );

        return memberLinkResponse;
    }

    protected List<InternalMemberProfileSpecificResponse.MemberLinkResponse> memberLinkListToMemberLinkResponseList3(List<MemberLink> list) {
        if ( list == null ) {
            return null;
        }

        List<InternalMemberProfileSpecificResponse.MemberLinkResponse> list1 = new ArrayList<InternalMemberProfileSpecificResponse.MemberLinkResponse>( list.size() );
        for ( MemberLink memberLink : list ) {
            list1.add( memberLinkToMemberLinkResponse3( memberLink ) );
        }

        return list1;
    }

    protected InternalMemberProfileSpecificResponse.MemberCareerResponse memberCareerToMemberCareerResponse4(MemberCareer memberCareer) {
        if ( memberCareer == null ) {
            return null;
        }

        Long id = null;
        String companyName = null;
        String title = null;
        String startDate = null;
        String endDate = null;
        Boolean isCurrent = null;

        id = memberCareer.getId();
        companyName = memberCareer.getCompanyName();
        title = memberCareer.getTitle();
        startDate = memberCareer.getStartDate();
        endDate = memberCareer.getEndDate();
        isCurrent = memberCareer.getIsCurrent();

        InternalMemberProfileSpecificResponse.MemberCareerResponse memberCareerResponse = new InternalMemberProfileSpecificResponse.MemberCareerResponse( id, companyName, title, startDate, endDate, isCurrent );

        return memberCareerResponse;
    }

    protected List<InternalMemberProfileSpecificResponse.MemberCareerResponse> memberCareerListToMemberCareerResponseList4(List<MemberCareer> list) {
        if ( list == null ) {
            return null;
        }

        List<InternalMemberProfileSpecificResponse.MemberCareerResponse> list1 = new ArrayList<InternalMemberProfileSpecificResponse.MemberCareerResponse>( list.size() );
        for ( MemberCareer memberCareer : list ) {
            list1.add( memberCareerToMemberCareerResponse4( memberCareer ) );
        }

        return list1;
    }

    protected InternalMemberProfileSpecificResponse.MemberProjectResponse memberProfileProjectDaoToMemberProjectResponse1(MemberProfileProjectDao memberProfileProjectDao) {
        if ( memberProfileProjectDao == null ) {
            return null;
        }

        Long id = null;
        String name = null;
        String summary = null;
        Integer generation = null;
        String category = null;
        String logoImage = null;
        String thumbnailImage = null;
        String[] serviceType = null;

        id = memberProfileProjectDao.id();
        name = memberProfileProjectDao.name();
        summary = memberProfileProjectDao.summary();
        generation = memberProfileProjectDao.generation();
        category = memberProfileProjectDao.category();
        logoImage = memberProfileProjectDao.logoImage();
        thumbnailImage = memberProfileProjectDao.thumbnailImage();
        String[] serviceType1 = memberProfileProjectDao.serviceType();
        if ( serviceType1 != null ) {
            serviceType = Arrays.copyOf( serviceType1, serviceType1.length );
        }

        InternalMemberProfileSpecificResponse.MemberProjectResponse memberProjectResponse = new InternalMemberProfileSpecificResponse.MemberProjectResponse( id, name, summary, generation, category, logoImage, thumbnailImage, serviceType );

        return memberProjectResponse;
    }

    protected List<InternalMemberProfileSpecificResponse.MemberProjectResponse> memberProfileProjectDaoListToMemberProjectResponseList1(List<MemberProfileProjectDao> list) {
        if ( list == null ) {
            return null;
        }

        List<InternalMemberProfileSpecificResponse.MemberProjectResponse> list1 = new ArrayList<InternalMemberProfileSpecificResponse.MemberProjectResponse>( list.size() );
        for ( MemberProfileProjectDao memberProfileProjectDao : list ) {
            list1.add( memberProfileProjectDaoToMemberProjectResponse1( memberProfileProjectDao ) );
        }

        return list1;
    }
}
