package com.sys.managesys.common.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustMnpDto {
    private Long custId;
    private String isSameAsCust;
    private String mnpTelNo;
    private String ownerName;
    private String ownerSsnEnc;
    private String contactNo;
    private String issueDate;
    private String prevCarrier;
    private String mnpMemo;
    private String remark;
    private String useYn;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
