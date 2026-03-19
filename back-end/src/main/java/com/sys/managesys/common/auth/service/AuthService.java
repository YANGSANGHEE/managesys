package com.sys.managesys.common.auth.service;

import com.sys.managesys.common.config.CustomUserDetails;
import com.sys.managesys.common.config.JwtProvider;
import com.sys.managesys.common.dto.LoginRequest;
import com.sys.managesys.common.dto.LoginResponse;
import com.sys.managesys.common.dto.UserDto;
import com.sys.managesys.common.mapper.LoginHistoryMapper;
import com.sys.managesys.common.config.CustomUserDetailsService;
import com.sys.managesys.common.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String ACTION_LOGIN = "LOGIN";
    private static final String ACTION_LOGOUT = "LOGOUT";

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final LoginHistoryMapper loginHistoryMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService userDetailsService;

    /**
     * @param clientIp 로그인 요청 클라이언트 IP (null 가능)
     */
    public LoginResponse login(LoginRequest request, String clientIp) {
        // 로그인 토큰 생성 (아직 인증 전)
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        request.getLoginId(),
                        request.getPassword()
                );

        // 인증 시도 (여기서 모든 검증이 이루어짐)
        Authentication authentication = authenticationManager.authenticate(authToken);

        // 인증된 사용자 정보(Principal) 꺼내기
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UserDto userDto = userDetails.getUserDto();

        // PASSWORD_RESET_YN='Y' 이면 제한 토큰 발급 (change-password·logout 외 403)
        boolean mustChangePassword = "Y".equals(userDto.getPasswordResetYn());
        String accessToken = jwtProvider.createJwtToken(authentication, mustChangePassword);

        // 로그인 히스토리 저장 (TB_USER_LOGIN_HISTORY)
        try {
            loginHistoryMapper.insert(userDto.getUserId(), ACTION_LOGIN, clientIp);
        } catch (Exception e) {
            log.warn("로그인 히스토리 저장 실패: userId={}, ip={}, error={}", userDto.getUserId(), clientIp, e.getMessage());
        }

        return LoginResponse.builder()
                .accessToken(accessToken)               // 토큰
                .userId(userDto.getUserId())            // PK
                .loginId(userDto.getLoginId())          // 아이디
                .userName(userDto.getUserName())        // 이름
                .userRole(userDto.getUserRole())        // 권한
                .deptId(userDto.getDeptId())            // 부서
                .isLeader(userDto.getIsLeader())        // 리더 여부
                .mustChangePassword(mustChangePassword) // 비밀번호 재설정 필요 여부
                .build();
    }

    /**
     * 로그아웃 히스토리 저장 (로그아웃 API에서 호출)
     */
    public void recordLogout(Long userId, String clientIp) {
        if (userId == null) return;
        try {
            loginHistoryMapper.insert(userId, ACTION_LOGOUT, clientIp);
        } catch (Exception e) {
            log.warn("로그아웃 히스토리 저장 실패: userId={}, ip={}, error={}", userId, clientIp, e.getMessage());
        }
    }

    /**
     * 비밀번호 재설정 (초기화된 계정 로그인 후). 새 비밀번호로 변경하고 재설정 필수 플래그 해제.
     *
     * @throws IllegalArgumentException 새 비밀번호 불일치 또는 유효성 실패 시
     */
    public void changePassword(Long userId, String newPassword, String newPasswordConfirm) {
        if (userId == null) throw new IllegalArgumentException("사용자 정보가 없습니다.");
        if (newPassword == null || newPassword.isBlank())
            throw new IllegalArgumentException("새 비밀번호를 입력해 주세요.");
        if (!newPassword.equals(newPasswordConfirm))
            throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다.");
        if (newPassword.length() < 4)
            throw new IllegalArgumentException("비밀번호는 4자 이상으로 설정해 주세요.");

        String encoded = passwordEncoder.encode(newPassword);
        userMapper.updatePasswordAndClearReset(userId, encoded);
    }
}
