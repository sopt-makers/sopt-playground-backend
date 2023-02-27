package org.sopt.makers.internal.domain;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.config.AuthConfig;
import org.sopt.makers.internal.exception.WrongAccessTokenException;
import org.sopt.makers.internal.exception.WrongTokenException;
import org.sopt.makers.internal.service.CustomMemberDetailsService;
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
        val signatureAlgorithm= SignatureAlgorithm.HS256;
        val secretKeyBytes = DatatypeConverter.parseBase64Binary(authConfig.getJwtSecretKey());
        val signingKey = new SecretKeySpec(secretKeyBytes, signatureAlgorithm.getJcaName());
        val exp = new Date().toInstant().atZone(KST)
                .toLocalDateTime().plusDays(10).atZone(KST).toInstant();
        return Jwts.builder()
                .setSubject(Long.toString(userId))
                .setExpiration(Date.from(exp))
                .signWith(signingKey, signatureAlgorithm)
                .compact();
    }

    public boolean verifyAuthToken (String token) {
        try {
            val claims = getClaimsFromToken(token);

            val now = LocalDateTime.now(KST);
            val exp = claims.getExpiration().toInstant().atZone(KST).toLocalDateTime();
            if (exp.isBefore(now)) return false;

            return true;
        } catch (SignatureException e) {
            return false;
        }
    }

    private String getUserIdFromAuthToken (String token) {
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

    public Authentication getAuthentication(String token) {
        val userId = getUserIdFromAuthToken(token);
        val userDetails = memberDetailsService.loadUserByUsername(userId);
        return new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
    }

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

    public String verifyRegisterToken (String token) {
        try {
            val claims = getClaimsFromToken(token);

            val now = LocalDateTime.now(KST);
            val exp = claims.getExpiration().toInstant().atZone(KST).toLocalDateTime();
            if (exp.isBefore(now)) throw new WrongTokenException("잘못된 토큰입니다.");

            return claims.getAudience();
        } catch (SignatureException e) {
            throw new WrongAccessTokenException("Wrong signature is used");
        }
    }

    private Claims getClaimsFromToken (String token) throws SignatureException {
        return Jwts.parserBuilder()
                .setSigningKey(DatatypeConverter.parseBase64Binary(authConfig.getJwtSecretKey()))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}
