package org.sopt.makers.internal.external.sms;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.sopt.makers.internal.external.ExternalConfig;
import org.sopt.makers.internal.exception.BadRequestException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class InternalSmsClient {

    private final OkHttpClient smsOkHttpClient;
    private final ExternalConfig externalConfig;

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    public void sendSMS(String phone, String message) {
        int retryCount = 0;

        while (retryCount < 3) {
            try {
                sendSmsToInternalServer(phone, message);
                log.info("SMS 발송 성공 - 수신자: {}", phone);
                return;
            } catch (Exception e) {
                retryCount++;
                log.warn("SMS 발송 실패 - 재시도 {}/3, 수신자: {}, 에러: {}", retryCount, phone, e.getMessage());
            }
        }

        throw new BadRequestException("SMS 발송 재시도 초과");
    }

    private void sendSmsToInternalServer(String phone, String message) {
        String json = new Gson().toJson(Map.of("phone", phone, "message", message));

        RequestBody body = RequestBody.create(json, JSON_MEDIA_TYPE);

        Request request = new Request.Builder()
                .url(externalConfig.getSmsInternalUrl())
                .post(body)
                .addHeader("x-api-key", externalConfig.getSmsInternalApiKey())
                .build();

        try (Response response = smsOkHttpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "empty";

            if (!response.isSuccessful()) {
                log.error("Internal SMS 서버 응답 실패 - status: {}, body: {}", response.code(), responseBody);
                throw new BadRequestException("Internal SMS 서버 응답 실패 - status: " + response.code());
            }

            log.debug("Internal SMS 서버 응답 - body: {}", responseBody);
        } catch (IOException e) {
            log.error("Internal SMS 서버 통신 실패 - 수신자: {}", phone, e);
            throw new BadRequestException("Internal SMS 서버 통신 실패: " + e.getMessage());
        }
    }
}
