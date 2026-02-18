package com.sys.managesys.common.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserDto {
    private Long userId;          // USER_ID
    private String userName;
    private String userRole;
    private Integer isLeader;
    private String loginId;        // LOGIN_ID
    private String deptId;       // DEPT_ID
    private String password;   // PASSWORD
    private String useYn;           // USE_YN (Y/N)
    private String deptName;
}
