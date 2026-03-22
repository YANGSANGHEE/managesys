package com.sys.managesys.common.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 로그인/로그아웃 히스토리 DTO (TB_USER_LOGIN_HISTORY, EVENT_TYPE=LOGIN/LOGOUT)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginHistoryDto {
    private Long historyId;
    private Long userId;
    private String action;   // LOGIN, LOGOUT
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;
}
