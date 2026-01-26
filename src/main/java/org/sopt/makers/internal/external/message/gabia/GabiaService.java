package org.sopt.makers.internal.external.message.gabia;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.sopt.makers.internal.auth.AuthConfig;
import org.sopt.makers.internal.external.message.gabia.dto.GabiaAuthResponse;
import org.sopt.makers.internal.external.message.gabia.dto.GabiaSMSResponse;
import org.sopt.makers.internal.external.message.gabia.dto.GabiaSMSResponseData;
import org.sopt.makers.internal.exception.BadRequestException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GabiaService {

    private final AuthConfig authConfig;
    private final OkHttpClient gabiaOkHttpClient;

    private static final String SMS_OAUTH_TOKEN_URL = "https://sms.gabia.com/oauth/token";
    private static final String SMS_SEND_URL = "https://sms.gabia.com/api/send/sms";
    private static final String LMS_SEND_URL = "https://sms.gabia.com/api/send/lms";

    private GabiaAuthResponse getGabiaAccessToken() {
        String smsId = authConfig.getGabiaSMSId();
        String apiKey = authConfig.getGabiaApiKey();

        if (smsId == null || apiKey == null) {
            log.error("Gabia 설정값 누락 - smsId: {}, apiKey 존재 여부: {}", smsId, apiKey != null);
            throw new BadRequestException("Gabia 인증 설정이 누락되었습니다.");
        }

        String authValue = Base64.getEncoder()
                .encodeToString((smsId + ":" + apiKey).getBytes(StandardCharsets.UTF_8));

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("grant_type", "client_credentials")
                .build();

        Request request = new Request.Builder()
                .url(SMS_OAUTH_TOKEN_URL)
                .post(requestBody)
                .addHeader("Authorization", "Basic " + authValue)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        try (Response response = gabiaOkHttpClient.newCall(request).execute()) {
            String body = Objects.requireNonNull(response.body()).string();

            if (!response.isSuccessful()) {
                log.error("Gabia OAuth 실패 - status: {}, body: {}", response.code(), body);
                throw new BadRequestException("Gabia OAuth 인증 실패");
            }

            Map<String, String> result = new Gson().fromJson(body, HashMap.class);
            return new GabiaAuthResponse(result.get("access_token"));

        } catch (IOException e) {
            log.error("Gabia OAuth TLS 통신 실패", e);
            throw new BadRequestException("Gabia OAuth TLS 핸드셰이크 실패: " + e);
        }
    }

    public void sendSMS(String phone, String message) {
        int retryCount = 0;

        while (retryCount < 3) {
            GabiaSMSResponse response = attemptToSendSMS(phone, message);

            if ("200".equals(response.code())) {
                log.info("SMS 발송 성공 - 수신자: {}", phone);

                if (Integer.parseInt(response.data().getAFTER_SMS_QTY()) <= 50) {
                    // TODO Slack 알림
                }
                return;
            }

            retryCount++;
            log.warn("SMS 발송 실패 - 재시도 {}/3, 수신자: {}", retryCount, phone);
        }

        throw new BadRequestException("SMS 발송 재시도 초과");
    }

    private GabiaSMSResponse attemptToSendSMS(String phone, String message) {
        GabiaAuthResponse auth = getGabiaAccessToken();

        String authValue = Base64.getEncoder()
                .encodeToString((authConfig.getGabiaSMSId() + ":" + auth.access_token())
                        .getBytes(StandardCharsets.UTF_8));

        String targetUrl = message.length() <= 45 ? SMS_SEND_URL : LMS_SEND_URL;

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("phone", phone)
                .addFormDataPart("callback", authConfig.getGabiaSendNumber())
                .addFormDataPart("message", message)
                .addFormDataPart("refkey", UUID.randomUUID().toString())
                .build();

        Request request = new Request.Builder()
                .url(targetUrl)
                .post(requestBody)
                .addHeader("Authorization", "Basic " + authValue)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("cache-control", "no-cache")
                .build();

        try (Response response = gabiaOkHttpClient.newCall(request).execute()) {
            String body = Objects.requireNonNull(response.body()).string();
            Map<String, String> result = new Gson().fromJson(body, HashMap.class);
            return mapToGabiaSMSResponse(result);

        } catch (IOException e) {
            log.error("Gabia SMS TLS 통신 실패 - 수신자: {}", phone, e);
            throw new BadRequestException("Gabia SMS TLS 핸드셰이크 실패: " + e);
        }
    }

    private static GabiaSMSResponse mapToGabiaSMSResponse(Map<String, String> result) {
        if (!result.containsKey("code") || !result.containsKey("message")) {
            throw new BadRequestException("Gabia SMS 응답 파싱 실패");
        }

        String code = result.get("code");
        String message = result.get("message");

        GabiaSMSResponseData data = null;
        if (result.containsKey("data")) {
            data = new Gson().fromJson(new Gson().toJson(result.get("data")), GabiaSMSResponseData.class);
        }

        return new GabiaSMSResponse(code, message, data);
    }
}
