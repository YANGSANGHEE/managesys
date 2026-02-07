/* 권한 컨트롤러
*  로그인
* */

package com.sys.managesys.common.auth.controller;

import com.sys.managesys.common.auth.service.AuthService;
import com.sys.managesys.common.dto.LoginRequest;
import com.sys.managesys.common.dto.LoginResponse;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {
    @Resource(name = "authService")
    private final AuthService authService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /**
     * 비밀번호 해시 생성 유틸리티 (개발용)
     * 사용법: GET /api/auth/generate-hash?password=ocean123!
     */
//    @GetMapping("/generate-hash")
//    public Map<String, String> generateHash(@RequestParam String password) {
//        String hashedPassword = passwordEncoder.encode(password);
//        boolean matches = passwordEncoder.matches(password, hashedPassword);
//
//        Map<String, String> result = new HashMap<>();
//        result.put("originalPassword", password);
//        result.put("hashedPassword", hashedPassword);
//        return result;
//    }
}
