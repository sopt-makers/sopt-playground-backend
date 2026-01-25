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

import javax.net.ssl.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
        // SSL 디버깅 활성화
        System.setProperty("javax.net.debug", "ssl:handshake");

        try {
            // TrustManager 생성 - 모든 인증서 신뢰
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {
                            log.debug("checkClientTrusted: authType={}", authType);
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {
                            log.debug("checkServerTrusted: authType={}", authType);
                            if (chain != null && chain.length > 0) {
                                log.debug("Server cert: {}", chain[0].getSubjectDN());
                            }
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };

            // SSLContext 생성 - TLSv1.2 명시
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, trustAllCerts, new SecureRandom());

            // SSLSocketFactory 래핑하여 TLS 버전 강제
            SSLSocketFactory wrappedFactory = new SSLSocketFactory() {
                private final SSLSocketFactory delegate = sslContext.getSocketFactory();

                @Override
                public String[] getDefaultCipherSuites() {
                    return delegate.getDefaultCipherSuites();
                }

                @Override
                public String[] getSupportedCipherSuites() {
                    return delegate.getSupportedCipherSuites();
                }

                @Override
                public java.net.Socket createSocket(java.net.Socket s, String host, int port, boolean autoClose) throws IOException {
                    SSLSocket socket = (SSLSocket) delegate.createSocket(s, host, port, autoClose);
                    configureSocket(socket);
                    return socket;
                }

                @Override
                public java.net.Socket createSocket(String host, int port) throws IOException {
                    SSLSocket socket = (SSLSocket) delegate.createSocket(host, port);
                    configureSocket(socket);
                    return socket;
                }

                @Override
                public java.net.Socket createSocket(String host, int port, java.net.InetAddress localHost, int localPort) throws IOException {
                    SSLSocket socket = (SSLSocket) delegate.createSocket(host, port, localHost, localPort);
                    configureSocket(socket);
                    return socket;
                }

                @Override
                public java.net.Socket createSocket(java.net.InetAddress host, int port) throws IOException {
                    SSLSocket socket = (SSLSocket) delegate.createSocket(host, port);
                    configureSocket(socket);
                    return socket;
                }

                @Override
                public java.net.Socket createSocket(java.net.InetAddress address, int port, java.net.InetAddress localAddress, int localPort) throws IOException {
                    SSLSocket socket = (SSLSocket) delegate.createSocket(address, port, localAddress, localPort);
                    configureSocket(socket);
                    return socket;
                }

                private void configureSocket(SSLSocket socket) {
                    // TLS 1.2만 사용
                    socket.setEnabledProtocols(new String[]{"TLSv1.2"});

                    // 사용 가능한 모든 cipher suite 활성화
                    String[] supportedCiphers = socket.getSupportedCipherSuites();
                    log.debug("Supported cipher suites: {}", Arrays.toString(supportedCiphers));
                    socket.setEnabledCipherSuites(supportedCiphers);
                }
            };

            // OkHttpClient 설정
            this.client = new OkHttpClient.Builder()
                    .sslSocketFactory(wrappedFactory, (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier((hostname, session) -> {
                        log.debug("Verifying hostname: {}, session protocol: {}", hostname, session.getProtocol());
                        return true;
                    })
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            log.info("가비아 OkHttpClient 초기화 완료 (Custom SSL with all ciphers enabled)");

        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.error("SSL 설정 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("SSL 초기화 실패", e);
        }
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
                .addHeader("cache-control", "no-cache")
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
            log.error("가비아 통신 중 에러 (Token): {}", e.getMessage(), e);
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
                    if (Integer.parseInt(response.data().getAFTER_SMS_QTY()) <= 50) {
                        log.warn("가비아 SMS 잔량이 50건 이하입니다!");
                    }
                } else {
                    log.warn("SMS 발송 응답 에러 ({}회차): {}", retryCount + 1, response.message());
                    retryCount++;
                }
            } catch (Exception e) {
                log.error("SMS 발송 시도 중 예외 발생: {}", e.getMessage(), e);
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

        if (message.length() > 45) {
            bodyBuilder.addFormDataPart("subject", "SOPT");
        }

        RequestBody requestBody = bodyBuilder.build();

        Request request = new Request.Builder()
                .url(targetUrl)
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Authorization", "Basic " + authValue)
                .addHeader("cache-control", "no-cache")
                .build();

        try (Response response = client.newCall(request).execute()) {
            String bodyString = Objects.requireNonNull(response.body()).string();
            HashMap<String, String> result = gson.fromJson(bodyString, HashMap.class);
            return mapToGabiaSMSResponse(result);
        } catch (IOException e) {
            log.error("가비아 통신 중 에러 (Send): {}", e.getMessage(), e);
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