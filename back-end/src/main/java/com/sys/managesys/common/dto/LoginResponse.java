package com.sys.managesys.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/* 토큰 응답용 */
@Getter
@Builder // [핵심] 이 어노테이션이 있어야 .builder()를 사용할 수 있습니다.
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String accessToken;

    private Long userId;       // DB PK
    private String loginId;    // 로그인 아이디 (admin)
    private String userName;   // 사용자 이름 (홍길동)
    private String userRole;   // 권한 (ADMIN)
    private Long deptId;     // 부서 ID
    /** 비밀번호 초기화된 계정 여부 (true면 로그인 후 재설정 모달 표시) */
    private Boolean mustChangePassword;
}
