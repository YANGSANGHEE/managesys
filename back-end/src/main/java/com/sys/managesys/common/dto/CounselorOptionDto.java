package com.sys.managesys.common.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CounselorOptionDto {
    private Long userId;
    private String userName;
    private Long deptId;
}
