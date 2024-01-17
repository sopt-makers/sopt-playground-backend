package org.sopt.makers.internal.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "slackClient", url = "${slack.url}")
public interface SlackClient {
    @PostMapping(value = "${slack.newbie-channel}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    void postNewProfileMessage(@RequestBody String request);

    @PostMapping(value = "${slack.community-report-channel}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    void postReportMessage(@RequestBody String request);
}
