package com.sys.managesys.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/* 화면에서 유저 정보 조회해야할때 */
@Getter
@AllArgsConstructor
public class UserResponse {
    private Long userId;          // USER_ID
    private String userName;
    private String userRole;
    private Integer isLeader;
    private String loginId;        // LOGIN_ID
    private String deptId;       // DEPT_ID
    private String useYn;
}
