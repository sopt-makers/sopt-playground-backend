package org.sopt.makers.internal.external.sms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.makers.internal.external.message.gabia.GabiaService;
import org.sopt.makers.internal.external.sms.message.SmsMessageBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsNotificationService {

    private final GabiaService gabiaService;

    public void sendSms(SmsMessageBuilder messageBuilder) {
        try {
            String message = messageBuilder.buildMessage();
            String receiverPhone = messageBuilder.getReceiverPhone();

            log.info("SMS 발송 시작 - 수신자: {}", receiverPhone);
            gabiaService.sendSMS(receiverPhone, message);
            log.info("SMS 발송 완료 - 수신자: {}", receiverPhone);
        } catch (Exception e) {
            log.error("SMS 발송 실패 - 수신자: {}, 에러: {}", messageBuilder.getReceiverPhone(), e.getMessage(), e);
            throw e;
        }
    }
}
