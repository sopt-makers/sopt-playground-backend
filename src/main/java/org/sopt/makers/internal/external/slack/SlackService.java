package org.sopt.makers.internal.external.slack;

import com.slack.api.Slack;
import com.slack.api.model.Attachment;
import com.slack.api.model.Field;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.slack.api.webhook.WebhookPayloads.payload;

@Service
@Slf4j
public class SlackService {

    @Value("${slack.url}")
    private String slackUrl;

    @Value("${slack.playground-error-channel}")
    private String playgroundErrorChannel;

    private final Slack slackClient = Slack.getInstance();

    public void sendMessage(String title, HashMap<String, String> content, MessageType messageType) {
        StringBuilder sb = new StringBuilder();
        sb.append(slackUrl).append(playgroundErrorChannel);
        try {
            slackClient.send(sb.toString(), payload(c -> c
                    .text(title)
                    .attachments(List.of(
                            Attachment.builder().color(messageType.getColor())
                                    .fields( // 메시지 본문 내용
                                            content.keySet().stream().map(key -> generateSlackField(key, content.get(key))).collect(Collectors.toList())
                                    ).build()
                    ))
            ));
        } catch (IOException exception) {
            log.error(exception.getMessage(), exception);
        }
    }

    private Field generateSlackField(String title, String value) {
        return Field.builder()
                .title(title)
                .value(value)
                .valueShortEnough(false)
                .build();
    }
}

