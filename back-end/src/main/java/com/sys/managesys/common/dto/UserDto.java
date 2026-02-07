package com.sys.managesys.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserDto {
    private Long userId;          // USER_ID
    private String loginId;        // LOGIN_ID
    private String deptId;       // DEPT_ID
    private String password;   // PASSWORD
    private String useYn;           // USE_YN (Y/N)
}
