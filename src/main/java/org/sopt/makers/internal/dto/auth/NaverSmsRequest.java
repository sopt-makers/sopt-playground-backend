package org.sopt.makers.internal.dto.auth;

import java.util.List;

public record NaverSmsRequest (
        String type,
        String contentType,
        String countryCode,
        String from,
        String content,
        List<SmsMessage>messages
)
{
    public record SmsMessage(
       String to,
       String content
    ){}
}
