package org.sopt.makers.internal.common.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class HttpLoggingAspect {

    private final ObjectMapper objectMapper;
    private final SensitiveDataMasker sensitiveDataMasker;

    private static final Marker REQUEST_MARKER = MarkerFactory.getMarker("REQUEST");
    private static final Marker RESPONSE_MARKER = MarkerFactory.getMarker("RESPONSE");
    private static final Marker ERROR_MARKER = MarkerFactory.getMarker("ERROR");

    @Around("execution(* org.sopt.makers.internal..controller..*Controller.*(..))")
    public Object logApiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes()).getRequest();

        logRequest(request, joinPoint);
        Object result = null;
        try {
            result = joinPoint.proceed();
            logResponse(request, result, startTime);

            return result;
        } catch (Exception e) {
            logError(e, request, startTime);
            throw e;
        }
    }

    private void logRequest(HttpServletRequest request, ProceedingJoinPoint joinPoint) {
        try {
            Map<String, Object> requestLog = new HashMap<>();

            requestLog.put("method", request.getMethod());
            requestLog.put("uri", request.getRequestURI());
            requestLog.put("clientIp", getClientIp(request));

            if (request.getQueryString() != null) {
                requestLog.put("queryString", request.getQueryString());
            }

            Map<String, String> headers = extractHeaders(request);
            requestLog.put("headers", sensitiveDataMasker.maskHeaders(headers));

            Object[] args = joinPoint.getArgs();
            if (args != null) {
                for (Object arg : args) {
                    if (
                            arg != null &&
                                    !arg.getClass().getName().startsWith("javax.servlet") &&
                                    !arg.getClass().getName().startsWith("org.springframework")
                    ) {
                        String bodyJson = objectMapper.writeValueAsString(arg);
                        requestLog.put("body", sensitiveDataMasker.maskJsonString(bodyJson));
                        break;
                    }
                }
            }

            log.info(REQUEST_MARKER, objectMapper.writeValueAsString(requestLog));

        } catch (Exception e) {
            log.error("Request logging failed", e);
        }
    }

    private void logResponse(HttpServletRequest request, Object result, long startTime) {
        try {
            long duration = System.currentTimeMillis() - startTime;

            Map<String, Object> responseLog = new HashMap<>();

            responseLog.put("method", request.getMethod());
            responseLog.put("uri", request.getRequestURI());
            responseLog.put("duration", duration + "ms");

            if (result != null) {
                String responseJson = objectMapper.writeValueAsString(result);
                responseLog.put("body", sensitiveDataMasker.maskJsonString(responseJson));
            }

            log.info(RESPONSE_MARKER, objectMapper.writeValueAsString(responseLog));

        } catch (Exception e) {
            log.error("Response logging failed", e);
        }
    }

    private void logError(Exception exception, HttpServletRequest request, long startTime) {
        try {
            long duration = System.currentTimeMillis() - startTime;
            Map<String, Object> errorLog = new LinkedHashMap<>();

            errorLog.put("requestId", MDC.get("requestId"));
            errorLog.put("method", request.getMethod());
            errorLog.put("uri", request.getRequestURI());
            errorLog.put("duration", duration + "ms");
            errorLog.put("exceptionClass", exception.getClass().getSimpleName());
            errorLog.put("exceptionMessage", exception.getMessage());

            if (exception.getStackTrace().length > 0) {
                StackTraceElement firstTrace = exception.getStackTrace()[0];
                errorLog.put("errorLocation",
                        String.format("%s.%s:%d",
                                firstTrace.getClassName(),
                                firstTrace.getMethodName(),
                                firstTrace.getLineNumber()
                        )
                );
            } else {
                errorLog.put("errorLocation", "Stack trace not available");
            }

            log.error(ERROR_MARKER, objectMapper.writeValueAsString(errorLog));

        } catch (Exception e) {
            log.error("Error logging failed", e);
        }
    }

    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }

        return headers;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}
