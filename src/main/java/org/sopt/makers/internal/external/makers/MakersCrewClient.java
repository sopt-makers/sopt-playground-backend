package org.sopt.makers.internal.external.makers;

import org.sopt.makers.internal.member.dto.response.MemberCrewResponse;
import org.sopt.makers.internal.report.dto.CrewFastestJoinedGroupResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(value = "makersCrewClient", url = "${internal.crew.url}")
public interface MakersCrewClient {

    @GetMapping("/internal/meetings/post")
    CrewMeetingListResponse getMeetings(
            @RequestParam("page") Integer page,
            @RequestParam("take") Integer take
    );

    @GetMapping("/internal/post/{orgId}")
    CrewPostListResponse getPosts(
            @PathVariable("orgId") Long orgId,
            @RequestParam("page") Integer page,
            @RequestParam("take") Integer take
    );

    @GetMapping("/meeting/v2/org-user")
    MemberCrewResponse getUserAllCrew(
            @RequestParam("page") Integer page,
            @RequestParam("take") Integer take,
            @RequestParam("orgUserId") Long orgUserId
    );

    @GetMapping("/meeting/v2/{meetingId}/list")
    CrewMeetingApplicantListResponse getMeetingApplicants(
            @PathVariable("meetingId") Long meetingId,
            @RequestParam("page") Integer page,
            @RequestParam("take") Integer take,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "date", required = false) String date
    );

    @GetMapping("/internal/meetings/related-user-ids/{userId}")
    InternalUserWithMeetingUsersResponse getRelatedUserIds(
            @PathVariable("userId") Integer userId
    );

    @GetMapping("/internal/meeting/stats/fastest-applied/{orgId}")
    CrewFastestJoinedGroupResponse getFastestAppliedGroups(
            @PathVariable("orgId") Long orgUserId,
            @RequestParam("query-count") Integer queryCount,
            @RequestParam("query-year") Integer queryYear
    );
}
