package org.sopt.makers.internal.external.slack;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.makers.internal.external.slack.message.SlackMessageBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlackNotificationService {

    private final SlackClient slackClient;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    public void sendCommentReport(SlackMessageBuilder messageBuilder) {
        sendSlackMessage(messageBuilder, () -> slackClient.postReportMessage(messageBuilder.buildMessage().toString()));
    }

    public void sendNewProfile(SlackMessageBuilder messageBuilder) {
        sendSlackMessage(messageBuilder, () -> slackClient.postNewProfileMessage(messageBuilder.buildMessage().toString()));
    }

    public void sendNotMakers(SlackMessageBuilder messageBuilder) {
        sendSlackMessage(messageBuilder, () -> slackClient.postNotMakersMessage(messageBuilder.buildMessage().toString()));
    }

    private void sendSlackMessage(SlackMessageBuilder messageBuilder, Runnable sendAction) {
        try {
            if ("prod".equals(activeProfile)) {
                sendAction.run();
                log.info("슬랙 메시지 전송 성공: {}", messageBuilder.getClass().getSimpleName());
            } else {
                log.debug("슬랙 메시지 전송 스킵 (non-prod 환경): {}", messageBuilder.getClass().getSimpleName());
            }
        } catch (RuntimeException exception) {
            log.error("슬랙 메시지 전송 실패: {} - {}", messageBuilder.getClass().getSimpleName(), exception.getMessage(), exception);
        }
    }
}
