package com.sys.managesys.common.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustPaymentDto {
    private Long custId;
    private String payMethod;
    private String discountType;
    private String billType;
    private String bankCardName;
    private String accountCardNo;
    private String accountHolder;
    private String relationWithCust;
    private String holderSsnEnc;
    private String isSameAsCust;
    private String remark;
    private String useYn;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
