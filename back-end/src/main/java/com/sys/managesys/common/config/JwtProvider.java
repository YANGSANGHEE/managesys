/* 토큰생성 & 검증 책임 */

package com.sys.managesys.common.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProvider {
    /**
     * JWT 서명 키. app.jwt.secret(환경변수 APP_JWT_SECRET) 로 고정하면 서버를 재시작해도 기존 토큰이 유효하다.
     * 값이 없거나 32바이트 미만이면 부팅마다 랜덤 키로 폴백(개발 편의) → 이 경우 재시작 시 전체 토큰 무효화.
     * ⚠ 운영은 APP_JWT_SECRET 환경변수로 32바이트 이상의 안전한 값을 주입할 것.
     */
    @Value("${app.jwt.secret:}")
    private String jwtSecret;

    private SecretKey secretKey;
    private final long expireTime = 1000 * 60 * 60; // 토큰 1시간 유지

    @PostConstruct
    void initSecretKey() {
        if (jwtSecret != null && jwtSecret.getBytes(StandardCharsets.UTF_8).length >= 32) {
            this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        } else {
            // 설정 없음/너무 짧음 → 부팅마다 랜덤(재시작 시 토큰 무효화됨)
            this.secretKey = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        }
    }

    public static final String CLAIM_MUST_CHANGE_PASSWORD = "mustChangePassword";

    /** JWT 토큰 생성. mustChangePassword true면 재설정 전까지 다른 API 차단용 제한 토큰. */
    public String createJwtToken(Authentication authentication, boolean mustChangePassword) {
        Date now = new Date();
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        var builder = Jwts.builder()
                .setSubject(String.valueOf(user.getUserId()))
                .claim("role", user.getAuthorities())
                .claim(CLAIM_MUST_CHANGE_PASSWORD, mustChangePassword)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expireTime))
                .signWith(secretKey);
        return builder.compact();
    }

    /** 토큰에서 비밀번호 재설정 필수 여부 (제한 토큰 여부) */
    public Boolean getMustChangePasswordFromToken(String token) {
        if (token == null || token.isBlank()) return false;
        try {
            Claims body = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            Object val = body.get(CLAIM_MUST_CHANGE_PASSWORD);
            return Boolean.TRUE.equals(val);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 토큰에서 USER_ID 추출
    public Long getUserId(String token) {
        return Long.valueOf(
                Jwts.parserBuilder()
                        .setSigningKey(secretKey)
                        .build()
                        .parseClaimsJws(token)
                        .getBody()
                        .getSubject()
        );
    }

    // 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 만료된 토큰이어도 서명이 유효하면 USER_ID 추출 (로그아웃 히스토리 기록용).
     * 토큰 없음/서명 오류 시 null 반환.
     */
    public Long getUserIdFromTokenEvenIfExpired(String token) {
        if (token == null || token.isBlank()) return null;
        try {
            return Long.valueOf(
                    Jwts.parserBuilder()
                            .setSigningKey(secretKey)
                            .build()
                            .parseClaimsJws(token)
                            .getBody()
                            .getSubject()
            );
        } catch (ExpiredJwtException e) {
            String subject = e.getClaims().getSubject();
            if (subject == null || subject.isBlank()) return null;
            try {
                return Long.valueOf(subject);
            } catch (NumberFormatException nfe) {
                return null;
            }
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
