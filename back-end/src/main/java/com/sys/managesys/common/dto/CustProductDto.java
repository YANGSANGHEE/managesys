package com.sys.managesys.common.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustProductDto {
    private Long prodId;
    private Long custId;
    private String compName;
    private String prodGb;
    private String regionName;
    private String prodName;
    private String prodOpt;
    private String contractPeriod;
    private String stbType;
    private String vasName;
    private Integer lineCount;
    private String remark;
    private String useYn;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String subscriptionNo;
    private String openStatus;
    private LocalDate openDate;
    private LocalDate cancelDate;
}
