package com.sys.managesys.common.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustGiftDto {
    private Long custId;
    private String giftGb;
    private String giftName;
    private Integer giftAmount;
    private Integer addGiftAmount;
    private LocalDate paySchedDate;
    private String paySource;
    private LocalDate payDoneDate;
    private String bankName;
    private String accountNo;
    private String accountHolder;
    private String holderSsnEnc;
    private String isSameAsBank;
    private String remark;
    private String useYn;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
