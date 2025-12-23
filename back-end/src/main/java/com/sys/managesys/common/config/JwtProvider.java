/* 토큰생성 & 검증 책임 */

package com.sys.managesys.common.config;

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

    //JWT 토큰 생성
    public String createJwtToken(Authentication authentication) {
        Date now = new Date();

        CustomUserDetails user =
                (CustomUserDetails) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject(String.valueOf(user.getUserId()))   // USER_ID
                .claim("role", user.getRoleCode())              // ROLE_CODE
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expireTime))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .compact();
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
}
