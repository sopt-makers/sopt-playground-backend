package org.sopt.makers.internal.common.config;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class OkHttpConfig {

    @Bean
    public OkHttpClient gabiaOkHttpClient() {
        return new OkHttpClient.Builder()
                .connectionSpecs(Arrays.asList(
                        ConnectionSpec.MODERN_TLS,
                        ConnectionSpec.COMPATIBLE_TLS
                ))
                .build();
    }
}

