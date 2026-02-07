package com.sys.managesys.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/* 화면에서 유저 정보 조회해야할때 */
@Getter
@AllArgsConstructor
public class UserResponse {
    private Long userId;
    private String loginId;
    private String userName;
    private String deptId;
}
