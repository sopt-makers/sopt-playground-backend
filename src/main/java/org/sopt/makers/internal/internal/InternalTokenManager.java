//package org.sopt.makers.internal.internal;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.ExpiredJwtException;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import io.jsonwebtoken.security.SignatureException;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.util.Date;
//import lombok.RequiredArgsConstructor;
//import lombok.val;
//import org.sopt.makers.internal.auth.AuthConfig;
//import org.sopt.makers.internal.exception.WrongAccessTokenException;
//import org.sopt.makers.internal.exception.WrongTokenException;
//import org.springframework.stereotype.Service;
//
//import jakarta.crypto.spec.SecretKeySpec;
//import jakarta.xml.bind.DatatypeConverter;
//
//@RequiredArgsConstructor
//@Service
//public class InternalTokenManager {
//
//    private final AuthConfig authConfig;
////    private final CustomMemberDetailsService memberDetailsService;
//
//    private final ZoneId KST = ZoneId.of("Asia/Seoul");
//
//    public String createAuthToken(Long userId) {
//        val signatureAlgorithm= SignatureAlgorithm.HS256;
//        val secretKeyBytes = DatatypeConverter.parseBase64Binary(authConfig.getJwtSecretKey());
//        val signingKey = new SecretKeySpec(secretKeyBytes, signatureAlgorithm.getJcaName());
//        val exp = new Date().toInstant().atZone(KST)
//                .toLocalDateTime().plusDays(7).atZone(KST).toInstant();
//        return Jwts.builder()
//                .setSubject(Long.toString(userId))
//                .setExpiration(Date.from(exp))
//                .signWith(signingKey, signatureAlgorithm)
//                .compact();
//    }
//
//    public String createAuthToken(Long userId, long days, String serviceName) {
//        val signatureAlgorithm= SignatureAlgorithm.HS256;
//        val secretKeyBytes = DatatypeConverter.parseBase64Binary(authConfig.getJwtSecretKey());
//        val signingKey = new SecretKeySpec(secretKeyBytes, signatureAlgorithm.getJcaName());
//        val exp = new Date().toInstant().atZone(KST)
//                .toLocalDateTime().plusDays(days).atZone(KST).toInstant();
//        return Jwts.builder()
//                .setAudience(serviceName)
//                .setSubject(Long.toString(userId))
//                .setExpiration(Date.from(exp))
//                .signWith(signingKey, signatureAlgorithm)
//                .compact();
//    }
//
//    public boolean verifyAuthToken (String token) {
//        try {
//            val claims = getClaimsFromToken(token);
//
//            val now = LocalDateTime.now(KST);
//            val exp = claims.getExpiration().toInstant().atZone(KST).toLocalDateTime();
//            if (exp.isBefore(now)) return false;
//
//            return true;
//        } catch (SignatureException | ExpiredJwtException e) {
//            return false;
//        }
//    }
//
//    public String getUserIdFromAuthToken (String token) {
//        try {
//            val claims = getClaimsFromToken(token);
//
//            val now = LocalDateTime.now(KST);
//            val exp = claims.getExpiration().toInstant().atZone(KST).toLocalDateTime();
//            if (exp.isBefore(now)) throw new WrongTokenException("잘못된 토큰입니다.");
//
//            return claims.getSubject();
//        } catch (SignatureException e) {
//            throw new WrongAccessTokenException("Wrong signature is used");
//        }
//    }
//
////    public Authentication getAuthentication(String token) {
////        val userId = getUserIdFromAuthToken(token);
////        val userDetails = memberDetailsService.loadUserByUsername(userId);
////        return new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
////    }
//// 인증중앙화 삭제 예정
//
//    public String createRegisterToken(String email) {
//        val signatureAlgorithm= SignatureAlgorithm.HS256;
//        val secretKeyBytes = DatatypeConverter.parseBase64Binary(authConfig.getJwtSecretKey());
//        val signingKey = new SecretKeySpec(secretKeyBytes, signatureAlgorithm.getJcaName());
//        val exp = new Date().toInstant().atZone(KST)
//                .toLocalDateTime().plusHours(6).atZone(KST).toInstant();
//        return Jwts.builder()
//                .setAudience(email)
//                .setExpiration(Date.from(exp))
//                .signWith(signingKey, signatureAlgorithm)
//                .compact();
//    }
//
//    public String createCode(Long userId) {
//        val signatureAlgorithm= SignatureAlgorithm.HS256;
//        val secretKeyBytes = DatatypeConverter.parseBase64Binary(authConfig.getSecretForCode());
//        val signingKey = new SecretKeySpec(secretKeyBytes, signatureAlgorithm.getJcaName());
//        val exp = new Date().toInstant().atZone(KST)
//                .toLocalDateTime().plusMinutes(1).atZone(KST).toInstant();
//        return Jwts.builder()
//                .setSubject(Long.toString(userId))
//                .setExpiration(Date.from(exp))
//                .signWith(signingKey, signatureAlgorithm)
//                .compact();
//    }
//
//    public Long getUserIdFromCode (String code) {
//        try {
//            val claims = getClaimsFromCode(code);
//
//            val now = LocalDateTime.now(KST);
//            val exp = claims.getExpiration().toInstant().atZone(KST).toLocalDateTime();
//            if (exp.isBefore(now)) throw new WrongTokenException("잘못된 코드입니다.");
//
//            return Long.parseLong(claims.getSubject());
//        } catch (SignatureException e) {
//            throw new WrongAccessTokenException("Wrong signature is used");
//        }
//    }
//
//    public String verifyRegisterToken (String token) {
//        try {
//            val claims = getClaimsFromToken(token);
//
//            val now = LocalDateTime.now(KST);
//            val exp = claims.getExpiration().toInstant().atZone(KST).toLocalDateTime();
//            if (exp.isBefore(now)) throw new WrongTokenException("잘못된 토큰입니다.");
//
//            return claims.getAudience();
//        } catch (SignatureException | ExpiredJwtException e) {
//            throw new WrongAccessTokenException("Wrong token is used");
//        }
//    }
//
//    public boolean verifyMagicRegisterToken (String token) {
//        return authConfig.getMagicRegisterToken().equals(token);
//    }
//
//    public boolean verifyDevMagicRegisterQaToken(String token) {
//        return authConfig.getDevRegisterQaToken().equals(token);
//    }
//
//    private Claims getClaimsFromToken (String token) throws SignatureException {
//        return Jwts.parserBuilder()
//                .setSigningKey(DatatypeConverter.parseBase64Binary(authConfig.getJwtSecretKey()))
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//    }
//
//    private Claims getClaimsFromCode (String code) throws SignatureException {
//        return Jwts.parserBuilder()
//                .setSigningKey(DatatypeConverter.parseBase64Binary(authConfig.getSecretForCode()))
//                .build()
//                .parseClaimsJws(code)
//                .getBody();
//    }
//}
