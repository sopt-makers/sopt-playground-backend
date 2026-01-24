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

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
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

        OkHttpClient client = createGabiaClient();

        RequestBody requestBody = new FormBody.Builder()
                .add("grant_type", "client_credentials")
                .build();

        Request request = new Request.Builder()
                .url(SMS_OAUTH_TOKEN_URL)
                .post(requestBody)
                .addHeader("Authorization", "Basic " + authValue)
                .addHeader("cache-control", "no-cache")
                .build();

        try {
            Response response = client.newCall(request).execute();
            String responseBody = Objects.requireNonNull(response.body()).string();

            HashMap<String, String> result = new Gson().fromJson(responseBody, HashMap.class);
            String accessToken = result.get("access_token");

            if (accessToken == null || accessToken.isEmpty()) {
                String errorMessage = result.get("message");
                log.error("Gabia 토큰 발급 실패: {}", errorMessage);
                throw new BadRequestException("Gabia SMS 토큰 발급 실패: " + errorMessage);
            }

            return new GabiaAuthResponse(accessToken);
        } catch (IOException e) {
            log.error("Gabia 토큰 발급 중 IOException 발생", e);
            throw new BadRequestException("Gabia에 잘못된 인증 데이터가 전달됐습니다.");
        }
    }

    public void sendSMS(String phone, String message) {
        boolean sentSuccessfully = false;
        int retryCount = 0;

        while (!sentSuccessfully && retryCount < 3) {
            GabiaSMSResponse response = attemptToSendSMS(phone, message);
            log.info("SMS 발송 응답: {}", response);

            if (response.code().equals("200")) {
                sentSuccessfully = true;

                if (Integer.parseInt(response.data().getAFTER_SMS_QTY()) == 50) {
                    // TODO: Slack에 알림 전송
                }
            } else {
                retryCount++;
                log.warn("SMS 발송 실패 (재시도 {}/3): {}", retryCount, response.message());
            }
        }

        if (!sentSuccessfully) {
            log.error("SMS 발송 3회 재시도 후 최종 실패 - phone: {}", phone);
        }
    }

    private GabiaSMSResponse attemptToSendSMS(String phone, String message) {
        GabiaAuthResponse gabiaAuthResponse = getGabiaAccessToken();
        String authValue = "Bearer " + gabiaAuthResponse.access_token();

        OkHttpClient client = new OkHttpClient();

        String targetUrl;
        if (message.length() <= 45) {
            targetUrl = SMS_SEND_URL;
        } else {
            targetUrl = LMS_SEND_URL;
        }

        RequestBody requestBody = new FormBody.Builder()
                .add("phone", phone)
                .add("callback", authConfig.getGabiaSendNumber())
                .add("message", message)
                .add("refkey", UUID.randomUUID().toString())
                .build();

        Request request = new Request.Builder()
                .url(targetUrl)
                .post(requestBody)
                .addHeader("Authorization", authValue)
                .addHeader("cache-control", "no-cache")
                .build();

        try {
            Response response = client.newCall(request).execute();
            String responseBody = Objects.requireNonNull(response.body()).string();

            HashMap<String, String> result = new Gson().fromJson(responseBody, HashMap.class);
            return mapToGabiaSMSResponse(result);
        } catch (IOException e) {
            log.error("SMS 발송 중 IOException 발생", e);
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

    private OkHttpClient createGabiaClient() {
        try {
            // TLSv1.2를 명시적으로 활성화
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, null, null);

            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.getSocketFactory(),
                            (X509TrustManager) TrustManagerFactory
                                    .getInstance(TrustManagerFactory.getDefaultAlgorithm())
                                    .getTrustManagers()[0])
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();
        } catch (Exception e) {
            log.error("OkHttpClient 생성 실패", e);
            return new OkHttpClient();
        }
    }
}