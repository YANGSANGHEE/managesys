package com.sys.managesys.common.dto;

import lombok.Getter;

/* 로그인 요청용 DTO */
@Getter
public class LoginRequest {
    private String loginId;
    private String password;
}
