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
    private static final String SMS_OAUTH_TOKEN_URL = "https://sms.gabia.com/oauth/token";
    private static final String SMS_SEND_URL = "https://sms.gabia.com/api/send/sms";
    private static final String LMS_SEND_URL = "https://sms.gabia.com/api/send/lms";

    private GabiaAuthResponse getGabiaAccessToken() {
        String authValue = Base64.getEncoder().encodeToString(
                String.format("%s:%s", authConfig.getGabiaSMSId(), authConfig.getGabiaApiKey())
                        .getBytes(StandardCharsets.UTF_8)
        );

        log.info("SMS ID: {}", authConfig.getGabiaSMSId());
        log.info("API Key 길이: {}", authConfig.getGabiaApiKey() != null ? authConfig.getGabiaApiKey().length() : 0);
        log.info("Basic Auth 값 (첫 20자): {}", authValue.substring(0, Math.min(20, authValue.length())));

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("grant_type", "client_credentials")
                .build();
        Request request = new Request.Builder()
                .url(SMS_OAUTH_TOKEN_URL)
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Authorization", "Basic " + authValue)
                .addHeader("cache-control", "no-cache")
                .build();

        try {
            log.info("Gabia 토큰 발급 요청 시작");
            Response response = client.newCall(request).execute();

            log.info("HTTP 상태 코드: {}", response.code());

            String responseBody = Objects.requireNonNull(response.body()).string();
            log.info("토큰 발급 응답 전체: {}", responseBody);

            HashMap<String, String> result = new Gson().fromJson(responseBody, HashMap.class);

            // 응답에 있는 모든 키 출력
            log.info("응답 키 목록: {}", result.keySet());

            String accessToken = result.get("access_token");
            if (accessToken == null || accessToken.isEmpty()) {
                String errorMessage = result.get("message");
                log.error("Gabia 토큰 발급 실패 - 에러 메시지: {}", errorMessage);

                // 전체 응답을 다시 확인
                result.forEach((key, value) -> log.error("응답 내용 - {}: {}", key, value));

                throw new BadRequestException("Gabia SMS 토큰 발급 실패: " + errorMessage);
            }

            log.info("발급된 액세스 토큰 길이: {}", accessToken.length());
            log.info("발급된 액세스 토큰 (첫 20자): {}", accessToken.substring(0, Math.min(20, accessToken.length())));

            return new GabiaAuthResponse(accessToken);
        } catch (IOException e) {
            log.error("Gabia 토큰 발급 중 IOException 발생", e);
            throw new BadRequestException("Gabia에 잘못된 인증 데이터가 전달됐습니다.");
        } catch (Exception e) {
            log.error("Gabia 토큰 발급 중 예상치 못한 에러 발생", e);
            throw new BadRequestException("Gabia에 잘못된 인증 데이터가 전달됐습니다.");
        }
    }

    public void sendSMS(String phone, String message) {
        boolean sentSuccessfully = false;
        int retryCount = 0;

        // 문자 발송이 실패한 경우 3번까지 재시도
        while (!sentSuccessfully && retryCount < 3) {
            GabiaSMSResponse response = attemptToSendSMS(phone, message);

            if (response.code().equals("200")) {
                sentSuccessfully = true;

                // TODO:Slack에 알림 전송
                if (Integer.parseInt(response.data().getAFTER_SMS_QTY()) == 50) {

                }

            } else {
                retryCount++;
            }
        }
    }

    private GabiaSMSResponse attemptToSendSMS(String phone, String message) {
        GabiaAuthResponse gabiaAuthResponse = getGabiaAccessToken();
        String authValue = Base64.getEncoder().encodeToString(String.format("%s:%s", authConfig.getGabiaSMSId(), gabiaAuthResponse.access_token()).getBytes(StandardCharsets.UTF_8));
        OkHttpClient client = new OkHttpClient();

        String targetUrl;
        if (message.length() <= 45) {
            targetUrl = SMS_SEND_URL;
        } else {
            targetUrl = LMS_SEND_URL;
        }

        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("phone", phone)
                .addFormDataPart("callback", authConfig.getGabiaSendNumber())
                .addFormDataPart("message", message)
                .addFormDataPart("refkey", UUID.randomUUID().toString())
                .build();

        Request request = new Request.Builder()
                .url(targetUrl)
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Authorization", "Basic " + authValue)
                .addHeader("cache-control", "no-cache")
                .build();

        try {
            Response response = client.newCall(request).execute();
            HashMap<String, String> result = new Gson().fromJson(Objects.requireNonNull(response.body()).string(), HashMap.class);
            return mapToGabiaSMSResponse(result);
        } catch (IOException e) {
            throw new BadRequestException("Gabia에 잘못된 인증 데이터가 전달됐습니다.");
        }
    }

    private static GabiaSMSResponse mapToGabiaSMSResponse(HashMap<String, String> result) {
        if (!result.containsKey("code") || !result.containsKey("message")) {
            throw new BadRequestException("Gabia 서버 통신에 실패했습니다");
        }

        String code = result.get("code");
        String message = result.get("message");
        String data = new Gson().toJson(result.get("data"));
        GabiaSMSResponseData gabiaSMSResponseData = new Gson().fromJson(data, GabiaSMSResponseData.class);

        return new GabiaSMSResponse(code, message, gabiaSMSResponseData);
    }
}
