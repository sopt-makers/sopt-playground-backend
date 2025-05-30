package org.sopt.makers.internal.external.naver;

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
