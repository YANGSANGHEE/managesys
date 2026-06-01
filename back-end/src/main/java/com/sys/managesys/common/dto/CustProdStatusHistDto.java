package com.sys.managesys.common.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustProdStatusHistDto {
    private Long histId;
    private Long custId;
    private Long prodId;
    private String openStatus;
    private String statusName;   // CUST_STATUS 공통코드 JOIN
    private Long changerId;
    private String changerName;  // TB_USER JOIN
    private LocalDateTime changedAt;
}
