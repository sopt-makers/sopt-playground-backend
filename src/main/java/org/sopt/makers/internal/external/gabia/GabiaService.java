package org.sopt.makers.internal.external.gabia;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.sopt.makers.internal.config.AuthConfig;
import org.sopt.makers.internal.dto.auth.GabiaAuthResponse;
import org.sopt.makers.internal.dto.auth.GabiaSMSResponse;
import org.sopt.makers.internal.dto.auth.GabiaSMSResponseData;
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GabiaService {

    private final AuthConfig authConfig;
    private static final String SMS_OAUTH_TOKEN_URL = "https://sms.gabia.com/oauth/token";
    private static final String SMS_SEND_URL = "https://sms.gabia.com/api/send/sms";

    private GabiaAuthResponse getGabiaAccessToken() {
        String authValue = Base64.getEncoder().encodeToString(String.format("%s:%s", authConfig.getGabiaSMSId(), authConfig.getGabiaApiKey()).getBytes(StandardCharsets.UTF_8));

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
            Response response = client.newCall(request).execute();
            HashMap<String, String> result = new Gson().fromJson(Objects.requireNonNull(response.body()).string(), HashMap.class);
            return new GabiaAuthResponse(result.get("access_token"));
        } catch (IOException e) {
            throw new ClientBadRequestException("Gabia에 잘못된 인증 데이터가 전달됐습니다.");
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

        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("phone", phone)
                .addFormDataPart("callback", authConfig.getGabiaSendNumber())
                .addFormDataPart("message", message)
                .addFormDataPart("refkey", UUID.randomUUID().toString())
                .build();

        Request request = new Request.Builder()
                .url(SMS_SEND_URL)
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
            throw new ClientBadRequestException("Gabia에 잘못된 인증 데이터가 전달됐습니다.");
        }
    }

    private static GabiaSMSResponse mapToGabiaSMSResponse(HashMap<String, String> result) {
        if (!result.containsKey("code") || !result.containsKey("message")) {
            throw new ClientBadRequestException("Gabia 서버 통신에 실패했습니다");
        }

        String code = result.get("code");
        String message = result.get("message");
        String data = new Gson().toJson(result.get("data"));
        GabiaSMSResponseData gabiaSMSResponseData = new Gson().fromJson(data, GabiaSMSResponseData.class);

        return new GabiaSMSResponse(code, message, gabiaSMSResponseData);
    }
}
