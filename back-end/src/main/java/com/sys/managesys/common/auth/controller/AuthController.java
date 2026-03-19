/* 권한 컨트롤러
*  로그인 / 로그아웃 (히스토리 DB 저장)
* */

package com.sys.managesys.common.auth.controller;

import com.sys.managesys.common.auth.service.AuthService;
import com.sys.managesys.common.config.CustomUserDetails;
import com.sys.managesys.common.config.JwtProvider;
import com.sys.managesys.common.dto.ChangePasswordRequest;
import com.sys.managesys.common.dto.LoginRequest;
import com.sys.managesys.common.dto.LoginResponse;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    @Resource(name = "authService")
    private final AuthService authService;
    private final JwtProvider jwtProvider;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static String getClientIp(HttpServletRequest request) {
        if (request == null) return null;
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return authService.login(request, getClientIp(httpRequest));
    }

    /**
     * 로그아웃 시 호출. Authorization 헤더의 토큰(만료됐어도 됨)에서 userId를 꺼내 TB_USER_LOGIN_HISTORY에 기록.
     * 세션 끊김(401) 시 프론트에서 한 번만 호출해도 기록되도록 permitAll + 만료 토큰 파싱 지원.
     */
    @PostMapping("/logout")
    public void logout(HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        String token = (authHeader != null && authHeader.startsWith("Bearer ")) ? authHeader.substring(7) : null;
        Long userId = jwtProvider.getUserIdFromTokenEvenIfExpired(token);
        if (userId != null) {
            authService.recordLogout(userId, getClientIp(httpRequest));
        }
    }

    /**
     * 비밀번호 재설정 (초기화된 계정 로그인 후). 인증된 사용자만 호출 가능.
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getUserId();
        try {
            authService.changePassword(userId, request.getNewPassword(), request.getNewPasswordConfirm());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * 비밀번호 해시 생성 유틸리티 (개발용)
     * 사용법: GET /api/auth/generate-hash?password=ocean123!
     */
    @GetMapping("/generate-hash")
    public Map<String, String> generateHash(@RequestParam String password) {
        String hashedPassword = passwordEncoder.encode(password);
        boolean matches = passwordEncoder.matches(password, hashedPassword);

        Map<String, String> result = new HashMap<>();
        result.put("originalPassword", password);
        result.put("hashedPassword", hashedPassword);
        return result;
    }
}
