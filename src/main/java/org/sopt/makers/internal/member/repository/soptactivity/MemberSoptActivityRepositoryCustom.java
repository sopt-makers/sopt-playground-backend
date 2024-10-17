package org.sopt.makers.internal.member.repository.soptactivity;

import org.sopt.makers.internal.member.repository.soptactivity.dto.SoptActivityInfoDto;

import java.util.List;

public interface MemberSoptActivityRepositoryCustom {

    List<SoptActivityInfoDto> findAllSoptActivitiesByMemberId(Long memberId);
}
