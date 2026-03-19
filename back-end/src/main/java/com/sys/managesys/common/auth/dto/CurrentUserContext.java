package com.sys.managesys.common.auth.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrentUserContext {
    private Long userId;
    private String loginId;
    private String userRole;
    private Long deptId;
}
