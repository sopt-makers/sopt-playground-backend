package org.sopt.makers.internal.common.config;

import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionSpec;
import okhttp3.CipherSuite;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyStore;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class OkHttpConfig {

    static {
        // Java 17에서 비활성화된 TLS_RSA_* cipher suite를 가비아 서버 호환을 위해 재활성화
        String disabledAlgorithms = Security.getProperty("jdk.tls.disabledAlgorithms");
        if (disabledAlgorithms != null && disabledAlgorithms.contains("TLS_RSA_")) {
            String newValue = disabledAlgorithms.replace(", TLS_RSA_*", "").replace("TLS_RSA_*, ", "");
            Security.setProperty("jdk.tls.disabledAlgorithms", newValue);
        }
    }

    @Bean
    public OkHttpClient gabiaOkHttpClient() {
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            X509TrustManager trustManager = (X509TrustManager) trustManagers[0];

            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, new TrustManager[]{trustManager}, null);

            // 가비아 서버가 요구하는 TLS_RSA_WITH_AES_256_CBC_SHA를 포함한 SSLSocketFactory
            SSLSocketFactory sslSocketFactory = new GabiaSSLSocketFactory(sslContext.getSocketFactory());

            ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
                    .tlsVersions(TlsVersion.TLS_1_2)
                    .cipherSuites(
                            CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA,
                            CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA,
                            CipherSuite.TLS_RSA_WITH_AES_256_GCM_SHA384,
                            CipherSuite.TLS_RSA_WITH_AES_128_GCM_SHA256,
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256
                    )
                    .build();

            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, trustManager)
                    .connectionSpecs(Arrays.asList(spec, ConnectionSpec.CLEARTEXT))
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();
        } catch (Exception e) {
            log.error("OkHttpClient SSL 설정 실패, 기본 클라이언트 사용", e);
            return new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();
        }
    }

    /**
     * 가비아 서버 호환을 위해 TLS_RSA_WITH_AES_256_CBC_SHA cipher suite를 활성화하는 SSLSocketFactory
     */
    private static class GabiaSSLSocketFactory extends SSLSocketFactory {
        private final SSLSocketFactory delegate;
        private final String[] enabledCipherSuites;

        GabiaSSLSocketFactory(SSLSocketFactory delegate) {
            this.delegate = delegate;
            // 기존 cipher suites에 가비아가 요구하는 cipher 추가
            List<String> ciphers = new ArrayList<>(Arrays.asList(delegate.getDefaultCipherSuites()));
            if (!ciphers.contains("TLS_RSA_WITH_AES_256_CBC_SHA")) {
                ciphers.add("TLS_RSA_WITH_AES_256_CBC_SHA");
            }
            if (!ciphers.contains("TLS_RSA_WITH_AES_128_CBC_SHA")) {
                ciphers.add("TLS_RSA_WITH_AES_128_CBC_SHA");
            }
            this.enabledCipherSuites = ciphers.toArray(new String[0]);
        }

        @Override
        public String[] getDefaultCipherSuites() {
            return enabledCipherSuites;
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return enabledCipherSuites;
        }

        @Override
        public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
            Socket socket = delegate.createSocket(s, host, port, autoClose);
            enableCipherSuites(socket);
            return socket;
        }

        @Override
        public Socket createSocket(String host, int port) throws IOException {
            Socket socket = delegate.createSocket(host, port);
            enableCipherSuites(socket);
            return socket;
        }

        @Override
        public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
            Socket socket = delegate.createSocket(host, port, localHost, localPort);
            enableCipherSuites(socket);
            return socket;
        }

        @Override
        public Socket createSocket(InetAddress host, int port) throws IOException {
            Socket socket = delegate.createSocket(host, port);
            enableCipherSuites(socket);
            return socket;
        }

        @Override
        public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
            Socket socket = delegate.createSocket(address, port, localAddress, localPort);
            enableCipherSuites(socket);
            return socket;
        }

        private void enableCipherSuites(Socket socket) {
            if (socket instanceof SSLSocket) {
                ((SSLSocket) socket).setEnabledCipherSuites(enabledCipherSuites);
            }
        }
    }
}

