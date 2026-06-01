package com.sys.managesys.common.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustConsultDto {
    private Long consultId;
    private Long custId;
    private String consultType;
    private String consultTypeName;
    private String content;
    private Long creatorId;
    private String creatorName;
    private LocalDateTime createdAt;
    private String useYn;
}
