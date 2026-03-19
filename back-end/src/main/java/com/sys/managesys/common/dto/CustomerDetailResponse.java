package com.sys.managesys.common.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDetailResponse {
    private CustomerDto customer;
    private List<CustProductDto> products;
    private CustPaymentDto payment;
    private List<CustGiftDto> gifts;
    private List<CustMnpDto> mnps;
}
