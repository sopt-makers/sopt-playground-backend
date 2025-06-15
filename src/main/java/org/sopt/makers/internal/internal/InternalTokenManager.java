package org.sopt.makers.internal.internal;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.common.auth.AuthConfig;
import org.sopt.makers.internal.common.CustomMemberDetailsService;
import org.sopt.makers.internal.exception.WrongAccessTokenException;
import org.sopt.makers.internal.exception.WrongTokenException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@RequiredArgsConstructor
@Service
public class InternalTokenManager {

    private final AuthConfig authConfig;
    private final CustomMemberDetailsService memberDetailsService;

    private final ZoneId KST = ZoneId.of("Asia/Seoul");

    public String createAuthToken(Long userId) {
        // 1. 서명 알고리즘을 HS256으로 지정
        val signatureAlgorithm= SignatureAlgorithm.HS256;

        // 2. JWT 비밀키를 Base64로 읽어서 서명키로 변환
        val secretKeyBytes = DatatypeConverter.parseBase64Binary(authConfig.getJwtSecretKey()); // 설정에서 jwt 비밀키 가져와서 base64로 변환
        val signingKey = new SecretKeySpec(secretKeyBytes, signatureAlgorithm.getJcaName());  // 비밀키와 알고리즘으로 서명키 생성

        // 3. 만료일을 현재 시간 기준 7일 뒤로 설정
        val exp = new Date().toInstant().atZone(KST)
                .toLocalDateTime().plusDays(7).atZone(KST).toInstant();

        // 4. JWT 토큰 생성 (userId를 subject로, 만료일, 서명 포함) 및 반환
        return Jwts.builder()
                .setSubject(Long.toString(userId))
                .setExpiration(Date.from(exp))
                .signWith(signingKey, signatureAlgorithm)
                .compact();
    }

    public String createAuthToken(Long userId, long days, String serviceName) {
        val signatureAlgorithm= SignatureAlgorithm.HS256;
        val secretKeyBytes = DatatypeConverter.parseBase64Binary(authConfig.getJwtSecretKey());
        val signingKey = new SecretKeySpec(secretKeyBytes, signatureAlgorithm.getJcaName());
        val exp = new Date().toInstant().atZone(KST)
                .toLocalDateTime().plusDays(days).atZone(KST).toInstant();
        return Jwts.builder()
                .setAudience(serviceName)
                .setSubject(Long.toString(userId))
                .setExpiration(Date.from(exp))
                .signWith(signingKey, signatureAlgorithm)
                .compact();
    }

    // JWT 토큰의 유효성(서명, 만료 시간)을 검증하는 메소드
    public boolean verifyAuthToken (String token) {
        try {
            val claims = getClaimsFromToken(token);

            val now = LocalDateTime.now(KST);
            val exp = claims.getExpiration().toInstant().atZone(KST).toLocalDateTime();
            if (exp.isBefore(now)) return false;

            return true;
        } catch (SignatureException | ExpiredJwtException e) {
            return false;
        }
    }

    // JWT 토큰에서 사용자 ID(subject)를 추출하며, 만료된 토큰이면 예외를 발생시키는 메소드
    public String getUserIdFromAuthToken (String token) {
        try {
            val claims = getClaimsFromToken(token);

            val now = LocalDateTime.now(KST);
            val exp = claims.getExpiration().toInstant().atZone(KST).toLocalDateTime();
            if (exp.isBefore(now)) throw new WrongTokenException("잘못된 토큰입니다.");

            return claims.getSubject();
        } catch (SignatureException e) {
            throw new WrongAccessTokenException("Wrong signature is used");
        }
    }

    // JWT 토큰으로부터 사용자 정보를 조회해 Spring Security 인증 객체를 생성하는 메소드
    public Authentication getAuthentication(String token) {
        val userId = getUserIdFromAuthToken(token);
        val userDetails = memberDetailsService.loadUserByUsername(userId);
        return new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
    }

    // 이메일을 대상(audience)으로 6시간 유효한 회원가입용 JWT 토큰을 생성하는 메소드
    public String createRegisterToken(String email) {
        val signatureAlgorithm= SignatureAlgorithm.HS256;
        val secretKeyBytes = DatatypeConverter.parseBase64Binary(authConfig.getJwtSecretKey());
        val signingKey = new SecretKeySpec(secretKeyBytes, signatureAlgorithm.getJcaName());
        val exp = new Date().toInstant().atZone(KST)
                .toLocalDateTime().plusHours(6).atZone(KST).toInstant();
        return Jwts.builder()
                .setAudience(email)
                .setExpiration(Date.from(exp))
                .signWith(signingKey, signatureAlgorithm)
                .compact();
    }

    // 사용자 ID를 subject로 1분간 유효한 코드(JWT)를 생성하는 메소드
    public String createCode(Long userId) {
        val signatureAlgorithm= SignatureAlgorithm.HS256;
        val secretKeyBytes = DatatypeConverter.parseBase64Binary(authConfig.getSecretForCode());
        val signingKey = new SecretKeySpec(secretKeyBytes, signatureAlgorithm.getJcaName());
        val exp = new Date().toInstant().atZone(KST)
                .toLocalDateTime().plusMinutes(1).atZone(KST).toInstant();
        return Jwts.builder()
                .setSubject(Long.toString(userId))
                .setExpiration(Date.from(exp))
                .signWith(signingKey, signatureAlgorithm)
                .compact();
    }

    // 코드(JWT)에서 사용자 ID를 추출하고, 만료 여부를 검증하는 메소드
    public Long getUserIdFromCode (String code) {
        try {
            val claims = getClaimsFromCode(code);

            val now = LocalDateTime.now(KST);
            val exp = claims.getExpiration().toInstant().atZone(KST).toLocalDateTime();
            if (exp.isBefore(now)) throw new WrongTokenException("잘못된 코드입니다.");

            return Long.parseLong(claims.getSubject());
        } catch (SignatureException e) {
            throw new WrongAccessTokenException("Wrong signature is used");
        }
    }

    // 회원가입 토큰(JWT)의 만료 여부를 검증하고, audience(이메일 등)를 반환하는 메소드
    public String verifyRegisterToken (String token) {
        try {
            val claims = getClaimsFromToken(token);

            val now = LocalDateTime.now(KST);
            val exp = claims.getExpiration().toInstant().atZone(KST).toLocalDateTime();
            if (exp.isBefore(now)) throw new WrongTokenException("잘못된 토큰입니다.");

            return claims.getAudience();
        } catch (SignatureException | ExpiredJwtException e) {
            throw new WrongAccessTokenException("Wrong token is used");
        }
    }

    public boolean verifyMagicRegisterToken (String token) {
        return authConfig.getMagicRegisterToken().equals(token);
    }

    public boolean verifyDevMagicRegisterQaToken(String token) {
        return authConfig.getDevRegisterQaToken().equals(token);
    }

    // JWT 토큰에서 claim 정보를 추출하는 내부 메소드
    private Claims getClaimsFromToken (String token) throws SignatureException {
        return Jwts.parserBuilder()
                .setSigningKey(DatatypeConverter.parseBase64Binary(authConfig.getJwtSecretKey()))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 코드(JWT)에서 claim 정보를 추출하는 내부 메소드
    private Claims getClaimsFromCode (String code) throws SignatureException {
        return Jwts.parserBuilder()
                .setSigningKey(DatatypeConverter.parseBase64Binary(authConfig.getSecretForCode()))
                .build()
                .parseClaimsJws(code)
                .getBody();
    }
}
