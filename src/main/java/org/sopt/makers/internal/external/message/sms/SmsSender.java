package org.sopt.makers.internal.external.message.sms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.Base64;

import lombok.AllArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.auth.AuthConfig;
import org.sopt.makers.internal.external.naver.NaverSmsRequest;
import org.sopt.makers.internal.external.naver.NaverSmsResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
@AllArgsConstructor
public class SmsSender {
    private final AuthConfig authConfig;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final RestTemplate smsClient = new RestTemplate();

    private final String baseUrl = "https://sens.apigw.ntruss.com/sms/v2/services/";

    private String makeSignature(String timestamp) {
        val space = " ";
        val newLine = "\n";
        val method = "POST";
        val url = "/sms/v2/services/"+ authConfig.getSmsServiceId()+"/messages";

        String message = new StringBuilder()
                .append(method)
                .append(space)
                .append(url)
                .append(newLine)
                .append(timestamp)
                .append(newLine)
                .append(authConfig.getSmsAccessKey())
                .toString();
        try {
            val signingKey = new SecretKeySpec(authConfig.getSmsSecretKey().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            val mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));

            return Base64.encodeBase64String(rawHmac);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public NaverSmsResponse sendSms(NaverSmsRequest.SmsMessage message) {
        val timestamp = String.valueOf(System.currentTimeMillis());

        val headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-ncp-apigw-timestamp", timestamp);
        headers.set("x-ncp-iam-access-key", authConfig.getSmsAccessKey());
        headers.set("x-ncp-apigw-signature-v2", makeSignature(timestamp));

        val messages = List.of(message);
        System.out.println("Phone " + authConfig.getSmsSenderPhone());
        val smsRequest = new NaverSmsRequest(
            "SMS", "COMM", "82",
                authConfig.getSmsSenderPhone(), message.content(), messages
        );

        try {
            val body = objectMapper.writeValueAsString(smsRequest);
            val httpBody = new HttpEntity<>(body, headers);

            return smsClient.postForObject(
                    new URI(
                            baseUrl
                                    + authConfig.getSmsServiceId()
                                    + "/messages"
                    ),
                    httpBody,
                    NaverSmsResponse.class);
        } catch (JsonProcessingException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
