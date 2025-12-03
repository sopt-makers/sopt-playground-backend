package org.sopt.makers.internal;

import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.time.Instant;

public class LambdaHandler implements RequestStreamHandler {

    private static SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        try {
            System.out.println("Starting Lambda handler initialization...");

            // 기본 방식으로 handler 생성
            handler = SpringBootLambdaContainerHandler.getAwsProxyHandler(InternalApplication.class);

        } catch (ContainerInitializationException e) {
            System.err.println("CRITICAL: Spring Boot application initialization failed: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Could not initialize Spring Boot application", e);
        } catch (Exception e) {
            System.err.println("CRITICAL: Unexpected error during initialization: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Could not initialize Lambda handler", e);
        }
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
            throws IOException {

        // 입력 스트림을 바이트 배열로 읽기
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int nRead;
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        byte[] byteArray = buffer.toByteArray();

        String inputString = new String(byteArray, StandardCharsets.UTF_8);

        // 원본 스트림 대신 사용할 새로운 스트림 생성
        ByteArrayInputStream newInputStream = new ByteArrayInputStream(byteArray);

        try {
            // JSON 파싱 시도
            Map<String, Object> event = objectMapper.readValue(inputString, Map.class);

            // Warmer 이벤트 확인
            if (event.containsKey("warmer") && Boolean.TRUE.equals(event.get("warmer"))) {
                System.out.println("Warmer event detected - keeping Lambda warm");
                String response = "{\"statusCode\":200,\"body\":\"Warmer event processed\"}";
                outputStream.write(response.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                return;
            }

            // CloudWatch Events 스케줄 이벤트 확인
            if (event.containsKey("source") && "aws.events".equals(event.get("source"))) {
                System.out.println("CloudWatch scheduled event detected");
                String response = "{\"statusCode\":200,\"body\":\"Scheduled event processed\"}";
                outputStream.write(response.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                return;
            }

            // detail-type이 있는 경우도 CloudWatch Events
            if (event.containsKey("detail-type")) {
                System.out.println("CloudWatch event detected: " + event.get("detail-type"));
                String response = "{\"statusCode\":200,\"body\":\"CloudWatch event processed\"}";
                outputStream.write(response.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                return;
            }

            // API Gateway 이벤트는 httpMethod가 있어야 함
            if (!event.containsKey("httpMethod") && !event.containsKey("requestContext")) {
                System.out.println("Non-API Gateway event detected, ignoring: " + inputString.substring(0, Math.min(inputString.length(), 200)));
                String response = "{\"statusCode\":200,\"body\":\"Non-API event ignored\"}";
                outputStream.write(response.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                return;
            }

        } catch (Exception e) {
            // JSON 파싱 실패는 정상적인 경우일 수 있음 (예: 바이너리 데이터)
            System.out.println("Could not parse as JSON, processing as API Gateway event: " + e.getMessage());
        }

        // API Gateway 이벤트로 처리
        System.out.println("Processing as API Gateway event");
        System.out.println("Request details: " + inputString.substring(0, Math.min(inputString.length(), 200)));

        try {
            // 먼저 응답을 버퍼에 캡처하여 디버깅
            ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream();

            // 새로운 InputStream을 사용하여 핸들러 호출 (responseBuffer를 사용)
            handler.proxyStream(newInputStream, responseBuffer, context);

            // 응답 확인
            byte[] responseBytes = responseBuffer.toByteArray();
            if (responseBytes.length > 0) {
                String responseStr = new String(responseBytes, StandardCharsets.UTF_8);
                System.out.println("Response received, length: " + responseBytes.length);
                System.out.println("Response preview: " + responseStr.substring(0, Math.min(responseStr.length(), 500)));
                outputStream.write(responseBytes);
            } else {
                System.err.println("WARNING: Empty response from Spring Boot handler");
                // 빈 응답일 경우 기본 응답
                String emptyResponse = "{\"statusCode\":200,\"headers\":{},\"body\":\"\"}";
                outputStream.write(emptyResponse.getBytes(StandardCharsets.UTF_8));
            }

            outputStream.flush();

        } catch (Exception e) {
            System.err.println("Error processing API Gateway request: " + e.getMessage());
            e.printStackTrace();

            // 에러 발생 시 에러 응답 반환
            String errorResponse = "{\"statusCode\":500,\"headers\":{\"Content-Type\":\"application/json\"},\"body\":\"{\\\"error\\\":\\\"" + e.getMessage() + "\\\"}\"}";
            outputStream.write(errorResponse.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        }
    }
}
