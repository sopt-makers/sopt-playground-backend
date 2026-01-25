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
        String smsId = authConfig.getGabiaSMSId();
        String apiKey = authConfig.getGabiaApiKey();

        // 1. 설정값 누락 확인 로그
        if (smsId == null || apiKey == null) {
            log.error("Gabia 설정값이 누락되었습니다. ID: {}, Key 존재여부: {}", smsId, apiKey != null);
        }

        String authValue = Base64.getEncoder().encodeToString(
                String.format("%s:%s", smsId, apiKey).getBytes(StandardCharsets.UTF_8));

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("grant_type", "client_credentials")
                .build();
        Request request = new Request.Builder()
                .url(SMS_OAUTH_TOKEN_URL)
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Authorization", "Basic " + authValue)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String bodyString = Objects.requireNonNull(response.body()).string();

            // 2. 성공하지 않았을 때의 상세 로그 기록
            if (!response.isSuccessful()) {
                log.error("Gabia 토큰 요청 실패. Status: {}, Body: {}", response.code(), bodyString);
                throw new BadRequestException("Gabia 인증 실패: " + response.code());
            }

            HashMap<String, String> result = new Gson().fromJson(bodyString, HashMap.class);
            return new GabiaAuthResponse(result.get("access_token"));
        } catch (IOException e) {
            // 3. 실제 Exception 메시지 로그 기록
            log.error("Gabia 통신 중 IOException 발생: {}", e.getMessage());
            throw new BadRequestException("Gabia 서버와 통신할 수 없습니다.");
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
