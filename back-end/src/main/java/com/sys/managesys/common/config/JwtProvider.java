/* 토큰생성 & 검증 책임 */

package com.sys.managesys.common.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtProvider {
    private final String jwtSecret =
            "managesys-secret-key-very-long-and-secure-32bytes";// 비밀키 ** .env에서 가져오도록 설정해놓을것
    private final long expireTime = 1000 * 60 * 60 ; // 토큰 1시간 유지

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
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()));
        return builder.compact();
    }

    /** 토큰에서 비밀번호 재설정 필수 여부 (제한 토큰 여부) */
    public Boolean getMustChangePasswordFromToken(String token) {
        if (token == null || token.isBlank()) return false;
        try {
            Claims body = Jwts.parserBuilder()
                    .setSigningKey(jwtSecret.getBytes())
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
                        .setSigningKey(jwtSecret.getBytes())
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
                    .setSigningKey(jwtSecret.getBytes())
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
                            .setSigningKey(jwtSecret.getBytes())
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
