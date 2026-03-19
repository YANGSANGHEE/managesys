package com.sys.managesys.common.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserDto {
    private Long userId;
    private String userName;
    private String userRole;
    private Integer isLeader;
    private String loginId;
    private Long deptId;
    private String password;
    private String useYn;
    private String deptName;
    /** 비밀번호 초기화 여부 (Y: 로그인 시 재설정 필요) */
    private String passwordResetYn;
}
