package com.sys.managesys.common.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 로그인 후 비밀번호 재설정(초기화된 계정) 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class ChangePasswordRequest {
    private String newPassword;
    private String newPasswordConfirm;
}
