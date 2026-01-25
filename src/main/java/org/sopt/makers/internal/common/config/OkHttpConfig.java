package org.sopt.makers.internal.common.config;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;
import okhttp3.CipherSuite;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Configuration
public class OkHttpConfig {

    @Bean
    public OkHttpClient gabiaOkHttpClient() {

        ConnectionSpec tlsSpec = new ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .cipherSuites(
                        // Gabia 서버가 실제로 쓰는 Cipher
                        CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA,

                        // 혹시를 대비한 하위 호환
                        CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA
                )
                .build();

        return new OkHttpClient.Builder()
                .connectionSpecs(Collections.singletonList(tlsSpec))
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }
}

