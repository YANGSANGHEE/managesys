package com.sys.managesys.common.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 비밀번호 변경 요청 DTO
 * - 강제 재설정(초기화된 계정): currentPassword 없이 사용
 * - 자발적 변경(헤더 모달): currentPassword 포함
 */
@Getter
@Setter
@NoArgsConstructor
public class ChangePasswordRequest {
    private String currentPassword;   // 현재 비밀번호 (자발적 변경 시 필수)
    private String newPassword;
    private String newPasswordConfirm;
}
