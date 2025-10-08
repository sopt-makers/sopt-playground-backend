package org.sopt.makers.internal.external.slack;

import com.slack.api.Slack;
import com.slack.api.model.Attachment;
import com.slack.api.model.Field;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.slack.api.webhook.WebhookPayloads.payload;

@Service
@Slf4j
public class SlackService {

    @Value("${slack.url}")
    private String slackUrl;

    @Value("${slack.playground-error-channel}")
    private String playgroundErrorChannel;

    private final static String NEW_LINE = "\n";

    private final Slack slackClient = Slack.getInstance();

    public void sendMessage(String title, HashMap<String, String> content, MessageType messageType, HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append(slackUrl).append(playgroundErrorChannel);
        try {
            String errorPointMessage = generateErrorPointMessage(request);

            slackClient.send(sb.toString(), payload(c -> c
                    .text(title)
                    .attachments(List.of(
                            Attachment.builder()
                                    .color(messageType.getColor())
                                    .fields( // 메시지 본문 내용
                                            Stream.concat(
                                                    Stream.of(generateSlackField("*🧾 Request Detail Info*", errorPointMessage)),
                                                    content.keySet().stream().map(key ->
                                                            generateSlackField(key, content.get(key)))
                                            ).collect(Collectors.toList())

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

    // HttpServletRequest를 사용하여 예외발생 요청에 대한 정보 메시지 생성
    private String generateErrorPointMessage(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();

        sb.setLength(0);
        sb.append("Request URL: " + request.getRequestURL().toString() + NEW_LINE);
        sb.append("Request Method: " + request.getMethod() + NEW_LINE);
        sb.append("Request Time : " + new Date() + NEW_LINE);

        return sb.toString();
    }
}

