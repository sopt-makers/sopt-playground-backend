package org.sopt.makers.internal.common.notification;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.external.slack.MessageType;
import org.sopt.makers.internal.external.slack.SlackService;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

@Service
@RequiredArgsConstructor
public class SlackErrorNotificationService implements ErrorNotificationService {

    private final SlackService slackService;

    @Override
    public void notifyError(Exception exception, HttpServletRequest request, String profile, String errorLocation) {
        LinkedHashMap<String, String> content = new LinkedHashMap<>();
        content.put("🌍 Environment", profile);
        content.put("📍 Error Location", errorLocation);
        content.put("⚠️ Exception Type", exception.getClass().getSimpleName());
        content.put("💬 Error Message", exception.getMessage());
        slackService.sendMessage(MessageType.SERVER.getTitle(), content, MessageType.SERVER, request);
    }

    @Override
    public boolean shouldNotify(Exception exception, String profile) {
        return !"prod".equals(profile);
    }
}
