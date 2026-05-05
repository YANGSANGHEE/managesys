package com.sys.managesys.common.mapper;

import com.sys.managesys.common.dto.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CustomerMapper {

    List<CustomerDto> selectCustomerList(CustomerDto searchDto);

    CustomerDto selectCustomerById(@Param("custId") Long custId);

    int insertCustomer(CustomerDto customer);

    int updateCustomer(CustomerDto customer);

    int updateVoucherReturnYn(@Param("custId") Long custId, @Param("voucherReturnYn") String voucherReturnYn);

    List<CustProductDto> selectProductsByCustId(@Param("custId") Long custId);

    CustPaymentDto selectPaymentByCustId(@Param("custId") Long custId);

    List<CustGiftDto> selectGiftsByCustId(@Param("custId") Long custId);

    List<CustMnpDto> selectMnpsByCustId(@Param("custId") Long custId);

    int insertPayment(CustPaymentDto payment);

    int insertGift(CustGiftDto gift);

    int insertProduct(CustProductDto product);

    int insertMnp(CustMnpDto mnp);

    int deletePaymentByCustId(@Param("custId") Long custId);

    int deleteGiftsByCustId(@Param("custId") Long custId);

    int deleteProductsByCustId(@Param("custId") Long custId);

    int deleteMnpsByCustId(@Param("custId") Long custId);

    /** 고객 삭제 (TB_CUSTOMER 삭제 시 FK ON DELETE CASCADE로 TB_CUST_* 테이블 연쇄 삭제) */
    int deleteByCustId(@Param("custId") Long custId);

    /** 그리드 인라인 편집용 개별 필드 업데이트
     *  상품 관련 필드는 PROD_ID 기준 (리스트가 상품 단위 행으로 분리됨).
     *  payDone(사은품)은 고객당 GENERAL 1건이므로 CUST_ID 기준 유지. */
    int quickUpdateSubscriptionNo(@Param("prodId") Long prodId, @Param("value") String value);
    int quickUpdateOpenDate(@Param("prodId") Long prodId, @Param("value") String value);
    int quickUpdateStatus(@Param("prodId") Long prodId, @Param("value") String value);
    int quickUpdatePayDone(@Param("custId") Long custId, @Param("value") String value);
}