package org.sopt.makers.internal.external;

import org.sopt.makers.internal.dto.member.MemberCrewResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "makersCrewProdClient", url = "https://crew.api.prod.sopt.org")
public interface MakersCrewProdClient {

    @GetMapping("/meeting/v2/org-user")
    MemberCrewResponse getUserAllCrew(
            @RequestParam("page") Integer page,
            @RequestParam("take") Integer take,
            @RequestParam("orgUserId") Long orgUserId
    );
}
