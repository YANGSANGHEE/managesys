package com.sys.managesys.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {
    private Long custId;
    private String status;
    private String custName;
    private String repName;
    private String custType;
    private String custKind;
    private String ssnEnc;
    private String bizNo;
    private String corpNo;
    private String foreignerRegNo;
    private String zipCode;
    private String addr;
    private String addrDetail;
    private String telNo;
    private String hpNo;
    private String hpCarrier;
    private String email;
    private String custAuthType;
    private String custAuthVal;
    private String counselorName;
    private Long deptId;
    private Long assignedUserId;
    private String remark;
    private String fileGroupId;
    private String voucherReturnYn;
    private String useYn;
    private Long creatorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String creatorName;
    private String assignedUserName;
    private String statusName;
    private String hpCarrierName;
    private String custTypeName;
    private String custKindName;
    private String prodName;
    private String prodType;
    private String setInfo;
    private String subscriptionNo;
    private String partner;
    private String acquirer;
    private String contractPeriod;
    private LocalDate openDate;
//    private LocalDate joinDate;      /* 가입일자 (JOIN_DATE) */
    private LocalDate authenticatedDate;    /* 인증일자 (AUTHENTICATED_DT) */
    private LocalDate receiptDate;   /* 접수일 (RECEIPT_DT) */
    private LocalDate cancellationDate; /* 해약일/해지일 (CANCELL_DT) */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDateTime installSchedDt;
    private String gift;
    private Integer amount;
    private Integer addGift;
    private String paySource;
    private LocalDate payDone;
    private String searchCustName;
    private String searchCounselorName;
    private String searchStatus;
    private String searchHpCarrier;
    private String searchCreatedAtFrom;
    private String searchCreatedAtTo;
    /** 기간 조회 기준: CREATED(등록일), RECEIPT(접수일), OPEN(개통일), JOIN(가입일), CANCEL(해지일) */
    private String searchPeriodType;
    private Long searchDeptId;
    private Long searchAssignedUserId;
    private Integer limit;

    /** 서버 전용: 권한/부서 필터 (리스트 조회 시 WHERE 조건) */
    @JsonIgnore
    private String filterUserRole;
    @JsonIgnore
    private Long filterUserId;
    @JsonIgnore
    private Long filterDeptId;
}
