package com.sys.managesys.common.auth.service;

import com.sys.managesys.common.config.JwtProvider;
import com.sys.managesys.common.dto.LoginRequest;
import com.sys.managesys.common.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    public LoginResponse login(LoginRequest request) {
        // 로그인 토큰 생성 (아직 인증 전)
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        request.getLoginId(),
                        request.getPassword()
                );

        // 인증 시도 (여기서 모든 검증이 이루어짐)
        Authentication authentication = authenticationManager.authenticate(authToken);

        // 인증 성공 JWT 발급 (USER_ID 기준)
        String accessToken = jwtProvider.createJwtToken(authentication);

        // 응답 DTO 반환
        return new LoginResponse(accessToken);
    }
}
