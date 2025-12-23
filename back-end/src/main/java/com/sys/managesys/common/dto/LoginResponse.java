package com.sys.managesys.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
/* 토큰 응답용 */
@Getter
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    //private String refreshToken;
}
