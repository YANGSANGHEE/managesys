package com.sys.managesys.common.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRegisterRequest {
    private CustomerDto customer;
    private String voucherReturnYn;
    private CustPaymentDto payment;
    private CustGiftDto gift;
    private CustGiftDto headGift;
    private List<CustProductDto> products;
    private CustMnpDto mnp;
}
