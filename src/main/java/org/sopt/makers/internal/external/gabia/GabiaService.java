package org.sopt.makers.internal.external.gabia;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.config.AuthConfig;
import org.sopt.makers.internal.dto.auth.GabiaAuthRequest;
import org.sopt.makers.internal.dto.auth.GabiaAuthResponse;
import org.sopt.makers.internal.dto.auth.GabiaSMSRequest;
import org.sopt.makers.internal.dto.auth.GabiaSMSResponse;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class GabiaService {

    private final AuthConfig authConfig;
    private final GabiaSMSClient gabiaSMSClient;

    private GabiaAuthResponse getGabiaAccessToken() {
        String authValue = Base64.getEncoder().encodeToString(String.format("%s:%s", authConfig.getGabiaSMSId(),
                authConfig.getGabiaApiKey()).getBytes(StandardCharsets.UTF_8));
        GabiaAuthRequest gabiaAuthRequest = new GabiaAuthRequest("client_credentials");

        return gabiaSMSClient.getGabiaAccessToken(
                "application/x-www-form-urlencoded",
                "Basic " + authValue,
                "no-cache",
                gabiaAuthRequest
        );
    }

    public void sendSMS(String phone, String message) {
        boolean sentSuccessfully = false;
        int retryCount = 0;

        // 문자 발송이 실패한 경우 3번까지 재시도
        while (!sentSuccessfully && retryCount < 3) {
            GabiaSMSResponse response = attemptToSendSMS(phone, message);

            if (response.code().equals("200")) {
                sentSuccessfully = true;

                // TODO: 문자 잔여량이 50이 되면 남으면 슬랙 채널에 발송
                if (Integer.parseInt(response.data().AFTRE_SMS_QTY()) == 50) {

                }
            } else {
                retryCount++;
            }
        }
    }

    private GabiaSMSResponse attemptToSendSMS(String phone, String message) {
        String accessToken = getGabiaAccessToken().access_token();
        String authValue = Base64.getEncoder().encodeToString(String.format("%s:%s", authConfig.getGabiaSMSId(),
                accessToken).getBytes(StandardCharsets.UTF_8));
        GabiaSMSRequest gabiaSMSRequest = new GabiaSMSRequest(phone, "callback", message, "refkey");

        return gabiaSMSClient.sendSMS(
                "application/x-www-form-urlencoded",
                "Basic " + authValue,
                gabiaSMSRequest
        );
    }
}
