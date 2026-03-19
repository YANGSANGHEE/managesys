package com.sys.managesys.common.auth.service;

import com.sys.managesys.common.auth.dto.CurrentUserContext;
import com.sys.managesys.common.dto.*;
import com.sys.managesys.common.mapper.CustomerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerMapper customerMapper;

    public List<CustomerDto> findCustomers(CustomerDto searchDto) {
        return customerMapper.selectCustomerList(searchDto);
    }

    public CustomerDetailResponse getCustomerDetail(Long custId, CurrentUserContext currentUser) {
        CustomerDto customer = customerMapper.selectCustomerById(custId);
        if (customer == null) return null;
        if (currentUser != null && !canAccessCustomer(customer, currentUser)) {
            throw new IllegalArgumentException("해당 고객 정보에 대한 조회 권한이 없습니다.");
        }
        List<CustProductDto> products = customerMapper.selectProductsByCustId(custId);
        CustPaymentDto payment = customerMapper.selectPaymentByCustId(custId);
        List<CustGiftDto> gifts = customerMapper.selectGiftsByCustId(custId);
        List<CustMnpDto> mnps = customerMapper.selectMnpsByCustId(custId);
        CustomerDetailResponse res = new CustomerDetailResponse();
        res.setCustomer(customer);
        res.setProducts(products != null ? products : new ArrayList<>());
        res.setPayment(payment);
        res.setGifts(gifts != null ? gifts : new ArrayList<>());
        res.setMnps(mnps != null ? mnps : new ArrayList<>());
        return res;
    }

    private boolean canAccessCustomer(CustomerDto customer, CurrentUserContext user) {
        boolean isAdmin = "admin".equalsIgnoreCase(user.getLoginId()) || "ADMIN".equalsIgnoreCase(user.getUserRole());
        if (isAdmin) return true;
        if ("MANAGER".equalsIgnoreCase(user.getUserRole()) && user.getDeptId() != null) {
            return user.getDeptId().equals(customer.getDeptId());
        }
        return user.getUserId() != null && user.getUserId().equals(customer.getAssignedUserId());
    }

    private static final int MAX_CUST_NAME = 50;
    private static final int MAX_REMARK = 2000;

    private void validateCustomer(CustomerDto customer, boolean forUpdate) {
        if (customer == null) throw new IllegalArgumentException("고객 정보가 없습니다.");
        if (customer.getCustName() == null || customer.getCustName().isBlank()) {
            throw new IllegalArgumentException("고객명을 입력해주세요.");
        }
        if (customer.getCustName().length() > MAX_CUST_NAME) {
            throw new IllegalArgumentException("고객명은 " + MAX_CUST_NAME + "자 이내로 입력해주세요.");
        }
        if (customer.getRemark() != null && customer.getRemark().length() > MAX_REMARK) {
            throw new IllegalArgumentException("비고는 " + MAX_REMARK + "자 이내로 입력해주세요.");
        }
        if (forUpdate && customer.getCustId() == null) {
            throw new IllegalArgumentException("고객 ID가 없습니다.");
        }
    }

    @Transactional
    public void register(CustomerRegisterRequest req) {
        CustomerDto customer = req.getCustomer();
        validateCustomer(customer, false);
        customer.setCustKind(customer.getCustKind() != null ? customer.getCustKind() : "INDIVIDUAL");
        customer.setStatus(customer.getStatus() != null ? customer.getStatus() : "RECEIPT");
        customer.setUseYn(customer.getUseYn() != null ? customer.getUseYn() : "Y");
        customerMapper.insertCustomer(customer);
        Long custId = customer.getCustId();
        if (custId == null) throw new IllegalStateException("고객 등록 후 CUST_ID를 확인할 수 없습니다.");
        if (req.getPayment() != null) {
            CustPaymentDto pay = req.getPayment();
            pay.setCustId(custId);
            pay.setUseYn(pay.getUseYn() != null ? pay.getUseYn() : "Y");
            customerMapper.insertPayment(pay);
        }
        if (req.getGift() != null) {
            CustGiftDto gift = req.getGift();
            gift.setCustId(custId);
            gift.setGiftGb("GENERAL");
            gift.setUseYn(gift.getUseYn() != null ? gift.getUseYn() : "Y");
            if (gift.getGiftAmount() == null) gift.setGiftAmount(0);
            if (gift.getAddGiftAmount() == null) gift.setAddGiftAmount(0);
            customerMapper.insertGift(gift);
        }
        if (req.getHeadGift() != null) {
            CustGiftDto head = req.getHeadGift();
            head.setCustId(custId);
            head.setGiftGb("HEAD");
            head.setUseYn(head.getUseYn() != null ? head.getUseYn() : "Y");
            if (head.getGiftAmount() == null) head.setGiftAmount(0);
            if (head.getAddGiftAmount() == null) head.setAddGiftAmount(0);
            customerMapper.insertGift(head);
        }
        if (req.getProducts() != null && !req.getProducts().isEmpty()) {
            for (CustProductDto prod : req.getProducts()) {
                prod.setCustId(custId);
                if (prod.getLineCount() == null) prod.setLineCount(1);
                customerMapper.insertProduct(prod);
            }
        }
        if (req.getMnp() != null) {
            CustMnpDto mnp = req.getMnp();
            mnp.setCustId(custId);
            mnp.setUseYn(mnp.getUseYn() != null ? mnp.getUseYn() : "Y");
            customerMapper.insertMnp(mnp);
        }
    }

    @Transactional
    public void update(CustomerRegisterRequest req, CurrentUserContext currentUser) {
        CustomerDto customer = req.getCustomer();
        validateCustomer(customer, true);
        Long custId = customer.getCustId();
        customer.setCustKind(customer.getCustKind() != null ? customer.getCustKind() : "INDIVIDUAL");
        customer.setUseYn(customer.getUseYn() != null ? customer.getUseYn() : "Y");
        String vyn = req.getVoucherReturnYn();
        if (vyn == null || vyn.isBlank()) vyn = customer.getVoucherReturnYn();
        String voucherReturnYn = (vyn != null && "Y".equalsIgnoreCase(vyn.trim())) ? "Y" : "N";
        customer.setVoucherReturnYn(voucherReturnYn);
        if (currentUser != null) {
            CustomerDto existing = customerMapper.selectCustomerById(custId);
            if (existing == null || !canAccessCustomer(existing, currentUser)) {
                throw new IllegalArgumentException("해당 고객 정보에 대한 수정 권한이 없습니다.");
            }
        }
        customerMapper.updateCustomer(customer);
        customerMapper.updateVoucherReturnYn(custId, voucherReturnYn);
        customerMapper.deletePaymentByCustId(custId);
        customerMapper.deleteGiftsByCustId(custId);
        customerMapper.deleteProductsByCustId(custId);
        customerMapper.deleteMnpsByCustId(custId);
        if (req.getPayment() != null) {
            CustPaymentDto pay = req.getPayment();
            pay.setCustId(custId);
            pay.setUseYn(pay.getUseYn() != null ? pay.getUseYn() : "Y");
            customerMapper.insertPayment(pay);
        }
        if (req.getGift() != null) {
            CustGiftDto gift = req.getGift();
            gift.setCustId(custId);
            gift.setGiftGb("GENERAL");
            gift.setUseYn(gift.getUseYn() != null ? gift.getUseYn() : "Y");
            if (gift.getGiftAmount() == null) gift.setGiftAmount(0);
            if (gift.getAddGiftAmount() == null) gift.setAddGiftAmount(0);
            customerMapper.insertGift(gift);
        }
        if (req.getHeadGift() != null) {
            CustGiftDto head = req.getHeadGift();
            head.setCustId(custId);
            head.setGiftGb("HEAD");
            head.setUseYn(head.getUseYn() != null ? head.getUseYn() : "Y");
            if (head.getGiftAmount() == null) head.setGiftAmount(0);
            if (head.getAddGiftAmount() == null) head.setAddGiftAmount(0);
            customerMapper.insertGift(head);
        }
        if (req.getProducts() != null && !req.getProducts().isEmpty()) {
            for (CustProductDto prod : req.getProducts()) {
                prod.setCustId(custId);
                if (prod.getLineCount() == null) prod.setLineCount(1);
                customerMapper.insertProduct(prod);
            }
        }
        if (req.getMnp() != null) {
            CustMnpDto mnp = req.getMnp();
            mnp.setCustId(custId);
            mnp.setUseYn(mnp.getUseYn() != null ? mnp.getUseYn() : "Y");
            customerMapper.insertMnp(mnp);
        }
    }

    /** 고객 삭제. 자식 테이블을 명시 삭제한 뒤 메인 삭제하여 고아 데이터(Orphan) 방지. (DB CASCADE 미설정 환경에서도 무결성 보장) */
    @Transactional
    public void deleteCustomer(Long custId, CurrentUserContext currentUser) {
        if (custId == null) throw new IllegalArgumentException("고객 ID가 없습니다.");
        CustomerDto customer = customerMapper.selectCustomerById(custId);
        if (customer == null) throw new IllegalArgumentException("해당 고객 정보를 찾을 수 없습니다.");
        if (currentUser != null && !canAccessCustomer(customer, currentUser)) {
            throw new IllegalArgumentException("해당 고객 정보에 대한 삭제 권한이 없습니다.");
        }
        customerMapper.deletePaymentByCustId(custId);
        customerMapper.deleteGiftsByCustId(custId);
        customerMapper.deleteProductsByCustId(custId);
        customerMapper.deleteMnpsByCustId(custId);
        customerMapper.deleteByCustId(custId);
    }
}
