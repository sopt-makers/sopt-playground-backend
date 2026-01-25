package org.sopt.makers.internal.external.message.gabia;

import com.google.gson.Gson;
import jakarta.annotation.PostConstruct;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class GabiaService {

    private final AuthConfig authConfig;
    private final Gson gson = new Gson();
    private OkHttpClient client;

    private static final String SMS_OAUTH_TOKEN_URL = "https://sms.gabia.com/oauth/token";
    private static final String SMS_SEND_URL = "https://sms.gabia.com/api/send/sms";
    private static final String LMS_SEND_URL = "https://sms.gabia.com/api/send/lms";

    @PostConstruct
    public void init() {
        this.client = new OkHttpClient();
    }

    private GabiaAuthResponse getGabiaAccessToken() {
        String smsId = authConfig.getGabiaSMSId();
        String apiKey = authConfig.getGabiaApiKey();

        if (smsId == null || apiKey == null) {
            log.error("가비아 인증 설정값이 비어있습니다.");
            throw new BadRequestException("가비아 설정 오류");
        }

        String authValue = Base64.getEncoder().encodeToString(
                String.format("%s:%s", smsId, apiKey).getBytes(StandardCharsets.UTF_8));

        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("grant_type", "client_credentials")
                .build();

        Request request = new Request.Builder()
                .url(SMS_OAUTH_TOKEN_URL)
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Authorization", "Basic " + authValue)
                .addHeader("cache-control", "no-cache")  // 추가된 헤더
                .build();

        try (Response response = client.newCall(request).execute()) {
            String bodyString = Objects.requireNonNull(response.body()).string();

            if (!response.isSuccessful()) {
                log.error("가비아 토큰 발급 실패: Status={}, Body={}", response.code(), bodyString);
                throw new BadRequestException("가비아 인증 실패");
            }

            HashMap<String, String> result = gson.fromJson(bodyString, HashMap.class);
            return new GabiaAuthResponse(result.get("access_token"));
        } catch (IOException e) {
            log.error("가비아 통신 중 에러 (Token): {}", e.getMessage());
            throw new BadRequestException("가비아 서버와 통신할 수 없습니다.");
        }
    }

    public void sendSMS(String phone, String message) {
        boolean sentSuccessfully = false;
        int retryCount = 0;

        while (!sentSuccessfully && retryCount < 3) {
            try {
                GabiaSMSResponse response = attemptToSendSMS(phone, message);
                if (response.code().equals("200")) {
                    sentSuccessfully = true;
                    // 잔량 부족 알림 등 후속 처리
                    if (Integer.parseInt(response.data().getAFTER_SMS_QTY()) <= 50) {
                        log.warn("가비아 SMS 잔량이 50건 이하입니다!");
                    }
                } else {
                    log.warn("SMS 발송 응답 에러 ({}회차): {}", retryCount + 1, response.message());
                    retryCount++;
                }
            } catch (Exception e) {
                log.error("SMS 발송 시도 중 예외 발생: {}", e.getMessage());
                retryCount++;
            }
        }
    }

    private GabiaSMSResponse attemptToSendSMS(String phone, String message) {
        GabiaAuthResponse authResponse = getGabiaAccessToken();
        String authValue = Base64.getEncoder().encodeToString(
                String.format("%s:%s", authConfig.getGabiaSMSId(), authResponse.access_token()).getBytes(StandardCharsets.UTF_8));

        String targetUrl = (message.length() <= 45) ? SMS_SEND_URL : LMS_SEND_URL;

        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("phone", phone)
                .addFormDataPart("callback", authConfig.getGabiaSendNumber())
                .addFormDataPart("message", message)
                .addFormDataPart("refkey", UUID.randomUUID().toString());

        // LMS인 경우 subject 추가 (공식 문서 참고)
        if (message.length() > 45) {
            bodyBuilder.addFormDataPart("subject", "SOPT");  // 필요시 제목 수정
        }

        RequestBody requestBody = bodyBuilder.build();

        Request request = new Request.Builder()
                .url(targetUrl)
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Authorization", "Basic " + authValue)
                .addHeader("cache-control", "no-cache")  // 추가된 헤더
                .build();

        try (Response response = client.newCall(request).execute()) {
            String bodyString = Objects.requireNonNull(response.body()).string();
            HashMap<String, String> result = gson.fromJson(bodyString, HashMap.class);
            return mapToGabiaSMSResponse(result);
        } catch (IOException e) {
            log.error("가비아 통신 중 에러 (Send): {}", e.getMessage());
            throw new BadRequestException("가비아 서버와 통신할 수 없습니다.");
        }
    }

    private GabiaSMSResponse mapToGabiaSMSResponse(HashMap<String, String> result) {
        if (!result.containsKey("code") || !result.containsKey("message")) {
            throw new BadRequestException("Gabia 응답 데이터 형식이 올바르지 않습니다.");
        }

        String code = result.get("code");
        String message = result.get("message");
        String dataJson = gson.toJson(result.get("data"));
        GabiaSMSResponseData data = gson.fromJson(dataJson, GabiaSMSResponseData.class);

        return new GabiaSMSResponse(code, message, data);
    }
}