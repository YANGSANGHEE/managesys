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
    private String passwordHash;   // PASSWORD_HASH
    private String roleCode;       // ROLE_CODE
    private String useAt;           // USE_AT (Y/N)
}
