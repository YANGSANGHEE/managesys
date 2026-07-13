package com.sys.managesys.common.auth.service;

import com.sys.managesys.common.auth.dto.CurrentUserContext;
import com.sys.managesys.common.config.AccessForbiddenException;
import com.sys.managesys.common.dto.*;
import com.sys.managesys.common.mapper.CustConsultMapper;
import com.sys.managesys.common.mapper.CustFileMapper;
import com.sys.managesys.common.mapper.CustProdStatusHistMapper;
import com.sys.managesys.common.mapper.CustomerMapper;
import com.sys.managesys.common.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerMapper customerMapper;
    private final UserMapper userMapper;
    private final CustConsultMapper custConsultMapper;
    private final CustProdStatusHistMapper custProdStatusHistMapper;
    private final CustFileMapper custFileMapper;
    private final FileStorageService fileStorageService;

    public List<CustomerDto> findCustomers(CustomerDto searchDto) {
        return customerMapper.selectCustomerList(searchDto);
    }

    public CustomerDetailResponse getCustomerDetail(Long custId, CurrentUserContext currentUser) {
        CustomerDto customer = customerMapper.selectCustomerById(custId);
        if (customer == null) return null;
        if (currentUser != null && !canAccessCustomer(customer, currentUser)) {
            throw new AccessForbiddenException("해당 고객 정보에 대한 조회 권한이 없습니다.");
        }
        List<CustProductDto> products = customerMapper.selectProductsByCustId(custId);
        CustPaymentDto payment = customerMapper.selectPaymentByCustId(custId);
        List<CustGiftDto> gifts = customerMapper.selectGiftsByCustId(custId);
        List<CustMnpDto> mnps = customerMapper.selectMnpsByCustId(custId);
        List<CustFileDto> attachments = custFileMapper.selectFilesByCustId(custId);
        CustomerDetailResponse res = new CustomerDetailResponse();
        res.setCustomer(customer);
        res.setProducts(products != null ? products : new ArrayList<>());
        res.setPayment(payment);
        res.setGifts(gifts != null ? gifts : new ArrayList<>());
        res.setMnps(mnps != null ? mnps : new ArrayList<>());
        res.setAttachments(attachments != null ? attachments : new ArrayList<>());
        return res;
    }

    /*
     * [전제] 아래 권한 검사들은 호출부에서 'currentUser != null' 가드 하에 수행된다.
     * 이는 방어적 패턴이며, 실제로는 SecurityConfig 가 /api/** 를 authenticated() 로 강제하므로
     * 컨트롤러 도달 시 currentUser 는 항상 non-null 이다.
     * ⚠ 향후 SecurityConfig 의 인증 정책을 완화하면 이 전제가 깨져 권한 검사가 우회될 수 있으니 주의.
     */

    /** 쓰기(수정/삭제/인라인수정) 권한: 최고 관리자(ADMIN) 및 팀장(MANAGER)만 허용. 일반 담당자(MEMBER)는 등록(Create)만 가능. */
    private boolean isWriter(CurrentUserContext user) {
        if (user == null) return false;
        return "ADMIN".equalsIgnoreCase(user.getUserRole())
                || "MANAGER".equalsIgnoreCase(user.getUserRole())
                || "admin".equalsIgnoreCase(user.getLoginId());
    }

    private boolean canAccessCustomer(CustomerDto customer, CurrentUserContext user) {
        boolean isAdmin = "admin".equalsIgnoreCase(user.getLoginId()) || "ADMIN".equalsIgnoreCase(user.getUserRole());
        if (isAdmin) return true;
        if ("MANAGER".equalsIgnoreCase(user.getUserRole()) && user.getDeptId() != null) {
            // 목록 스코프(CustomerMapper.xml MANAGER 분기)와 동일 규칙으로 통일한다.
            // 목록엔 보이지만 상세/수정에서 403 나던 불일치(미배정 팀 접수건 등)를 해소:
            //  ① 고객 담당부서(C.DEPT_ID)가 팀장 부서이거나
            //  ② 배정자(ASSIGNED_USER_ID)의 현재 부서가 팀장 부서이거나
            //  ③ 등록자(CREATOR_ID)의 현재 부서가 팀장 부서이면 접근 허용.
            Long deptId = user.getDeptId();
            if (deptId.equals(customer.getDeptId())) return true;
            if (customer.getAssignedUserId() != null) {
                UserDto assignedUser = userMapper.findByUserId(customer.getAssignedUserId());
                if (assignedUser != null && deptId.equals(assignedUser.getDeptId())) return true;
            }
            if (customer.getCreatorId() != null) {
                UserDto creator = userMapper.findByUserId(customer.getCreatorId());
                if (creator != null && deptId.equals(creator.getDeptId())) return true;
            }
            return false;
        }
        // MEMBER: 본인에게 배정된 고객만 접근
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
    public Long register(CustomerRegisterRequest req) {
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
            if (gift.getAddDepositAmount() == null) gift.setAddDepositAmount(0);
            customerMapper.insertGift(gift);
        }
        if (req.getHeadGift() != null) {
            CustGiftDto head = req.getHeadGift();
            head.setCustId(custId);
            head.setGiftGb("HEAD");
            head.setUseYn(head.getUseYn() != null ? head.getUseYn() : "Y");
            if (head.getGiftAmount() == null) head.setGiftAmount(0);
            if (head.getAddDepositAmount() == null) head.setAddDepositAmount(0);
            customerMapper.insertGift(head);
        }
        if (req.getProducts() != null && !req.getProducts().isEmpty()) {
            for (CustProductDto prod : req.getProducts()) {
                prod.setCustId(custId);
                if (prod.getLineCount() == null) prod.setLineCount(1);
                customerMapper.insertProduct(prod);
                // 최초 등록 개통상태 이력 기록
                String initStatus = prod.getOpenStatus() != null ? prod.getOpenStatus() : "RECEIPT";
                CustProdStatusHistDto hist = new CustProdStatusHistDto();
                hist.setCustId(custId);
                hist.setOpenStatus(initStatus);
                hist.setChangerId(customer.getCreatorId());
                custProdStatusHistMapper.insertHist(hist);
            }
        }
        if (req.getMnp() != null) {
            CustMnpDto mnp = req.getMnp();
            mnp.setCustId(custId);
            mnp.setUseYn(mnp.getUseYn() != null ? mnp.getUseYn() : "Y");
            customerMapper.insertMnp(mnp);
        }
        return custId;
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
        // 삭제 전 기존 상품 상태 스냅샷 (변경 감지용)
        List<CustProductDto> existingProducts = customerMapper.selectProductsByCustId(custId);
        if (currentUser != null) {
            if (!isWriter(currentUser)) {
                throw new AccessForbiddenException("고객 정보를 수정할 권한이 없습니다. (관리자/팀장 전용)");
            }
            CustomerDto existing = customerMapper.selectCustomerById(custId);
            if (existing == null || !canAccessCustomer(existing, currentUser)) {
                throw new AccessForbiddenException("해당 고객 정보에 대한 수정 권한이 없습니다.");
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
            if (gift.getAddDepositAmount() == null) gift.setAddDepositAmount(0);
            customerMapper.insertGift(gift);
        }
        if (req.getHeadGift() != null) {
            CustGiftDto head = req.getHeadGift();
            head.setCustId(custId);
            head.setGiftGb("HEAD");
            head.setUseYn(head.getUseYn() != null ? head.getUseYn() : "Y");
            if (head.getGiftAmount() == null) head.setGiftAmount(0);
            if (head.getAddDepositAmount() == null) head.setAddDepositAmount(0);
            customerMapper.insertGift(head);
        }
        if (req.getProducts() != null && !req.getProducts().isEmpty()) {
            for (int i = 0; i < req.getProducts().size(); i++) {
                CustProductDto prod = req.getProducts().get(i);
                prod.setCustId(custId);
                if (prod.getLineCount() == null) prod.setLineCount(1);
                customerMapper.insertProduct(prod);
                // 이전 상태와 비교해 변경된 경우에만 이력 기록
                String newStatus = prod.getOpenStatus() != null ? prod.getOpenStatus() : "RECEIPT";
                String oldStatus = (i < existingProducts.size() && existingProducts.get(i).getOpenStatus() != null)
                        ? existingProducts.get(i).getOpenStatus() : null;
                if (!newStatus.equals(oldStatus)) {
                    CustProdStatusHistDto hist = new CustProdStatusHistDto();
                    hist.setCustId(custId);
                    hist.setOpenStatus(newStatus);
                    hist.setChangerId(currentUser != null ? currentUser.getUserId() : null);
                    custProdStatusHistMapper.insertHist(hist);
                }
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
    public void quickUpdate(Long custId, Long prodId, String field, String value, CurrentUserContext currentUser) {
        if (custId == null) throw new IllegalArgumentException("고객 ID가 없습니다.");
        if (currentUser != null) {
            if (!isWriter(currentUser)) {
                throw new AccessForbiddenException("고객 정보를 수정할 권한이 없습니다. (관리자/팀장 전용)");
            }
            // update/delete 와 동일하게 부서 범위(팀장=자기 부서) 검증
            CustomerDto target = customerMapper.selectCustomerById(custId);
            if (target == null || !canAccessCustomer(target, currentUser)) {
                throw new AccessForbiddenException("해당 고객 정보에 대한 수정 권한이 없습니다.");
            }
        }
        switch (field) {
            case "subscriptionNo":
                if (prodId == null) throw new IllegalArgumentException("상품 ID가 없습니다.");
                customerMapper.quickUpdateSubscriptionNo(prodId, custId, value);
                break;
            case "openDate":
                if (prodId == null) throw new IllegalArgumentException("상품 ID가 없습니다.");
                customerMapper.quickUpdateOpenDate(prodId, custId, value);
                break;
            case "status":
                if (prodId == null) throw new IllegalArgumentException("상품 ID가 없습니다.");
                // 변경 전 상태 조회 → 실제로 값이 바뀐 경우에만 이력 기록 (무변경 저장 시 이력 오염 방지)
                String prevStatus = customerMapper.selectProductsByCustId(custId).stream()
                        .filter(p -> prodId.equals(p.getProdId()))
                        .map(CustProductDto::getOpenStatus)
                        .findFirst().orElse(null);
                customerMapper.quickUpdateStatus(prodId, custId, value);
                if (!java.util.Objects.equals(prevStatus, value)) {
                    CustProdStatusHistDto hist = new CustProdStatusHistDto();
                    hist.setCustId(custId);
                    hist.setProdId(prodId);
                    hist.setOpenStatus(value);
                    hist.setChangerId(currentUser != null ? currentUser.getUserId() : null);
                    custProdStatusHistMapper.insertHist(hist);
                }
                break;
            case "payDone":
                customerMapper.quickUpdatePayDone(custId, value);
                break;
            case "receiptDate":
                // 접수일은 고객(TB_CUSTOMER) 단위 컬럼이므로 prodId 불필요, CUST_ID 기준 갱신.
                // 빈 값은 접수일 해제(null)로 허용하고, 값이 있으면 yyyy-MM-dd(ISO) 형식만 허용해
                // 잘못된 문자열이 0000-00-00 등으로 저장되는 것을 방지한다.
                String receiptDt = (value == null || value.isBlank()) ? null : value.trim();
                if (receiptDt != null) {
                    try {
                        LocalDate.parse(receiptDt);
                    } catch (java.time.format.DateTimeParseException e) {
                        throw new IllegalArgumentException("접수일 형식이 올바르지 않습니다(yyyy-MM-dd): " + value);
                    }
                }
                customerMapper.quickUpdateReceiptDate(custId, receiptDt);
                break;
            default:
                throw new IllegalArgumentException("수정 불가능한 필드입니다: " + field);
        }
    }

    /** 고객 하위정보(상담/이력) 접근 권한 검증: 고객 미존재 400, 접근권한 없으면 403. (조회/추가 공통)
     *  /detail 과 동일한 canAccessCustomer 규칙을 적용해 타 고객 정보 무단 접근(IDOR)을 막는다. */
    private void requireCustomerAccess(Long custId, CurrentUserContext currentUser) {
        if (custId == null) throw new IllegalArgumentException("고객 ID가 없습니다.");
        CustomerDto customer = customerMapper.selectCustomerById(custId);
        if (customer == null) throw new IllegalArgumentException("해당 고객 정보를 찾을 수 없습니다.");
        if (currentUser != null && !canAccessCustomer(customer, currentUser)) {
            throw new AccessForbiddenException("해당 고객 정보에 대한 접근 권한이 없습니다.");
        }
    }

    public List<CustConsultDto> getConsults(Long custId, CurrentUserContext currentUser) {
        requireCustomerAccess(custId, currentUser);   // 타 고객 상담이력 무단 조회(IDOR) 방지
        return custConsultMapper.selectConsultsByCustId(custId);
    }

    public List<CustProdStatusHistDto> getStatusHist(Long custId, CurrentUserContext currentUser) {
        requireCustomerAccess(custId, currentUser);   // 타 고객 상태이력 무단 조회(IDOR) 방지
        return custProdStatusHistMapper.selectHistByCustId(custId);
    }

    @Transactional
    public void addConsult(CustConsultDto dto, CurrentUserContext currentUser) {
        if (dto.getCustId() == null) throw new IllegalArgumentException("고객 ID가 없습니다.");
        if (dto.getContent() == null || dto.getContent().isBlank()) throw new IllegalArgumentException("내용을 입력해주세요.");
        requireCustomerAccess(dto.getCustId(), currentUser);   // 타 고객에 무단 상담 추가(쓰기 IDOR) 방지
        custConsultMapper.insertConsult(dto);
    }

    /** 고객 삭제. 자식 테이블을 명시 삭제한 뒤 메인 삭제하여 고아 데이터(Orphan) 방지. (DB CASCADE 미설정 환경에서도 무결성 보장) */
    @Transactional
    public void deleteCustomer(Long custId, CurrentUserContext currentUser) {
        if (custId == null) throw new IllegalArgumentException("고객 ID가 없습니다.");
        CustomerDto customer = customerMapper.selectCustomerById(custId);
        if (customer == null) throw new IllegalArgumentException("해당 고객 정보를 찾을 수 없습니다.");
        if (currentUser != null) {
            if (!isWriter(currentUser)) {
                throw new AccessForbiddenException("고객 정보를 삭제할 권한이 없습니다. (관리자/팀장 전용)");
            }
            if (!canAccessCustomer(customer, currentUser)) {
                throw new AccessForbiddenException("해당 고객 정보에 대한 삭제 권한이 없습니다.");
            }
        }
        // 첨부파일: 디스크 실체를 먼저 지운 뒤(메타는 FK CASCADE로 연쇄 삭제되지만 명시적으로도 삭제)
        List<CustFileDto> files = custFileMapper.selectFilesByCustId(custId);
        for (CustFileDto f : files) {
            fileStorageService.deleteQuietly(f.getFilePath());
        }
        custFileMapper.deleteFilesByCustId(custId);
        customerMapper.deletePaymentByCustId(custId);
        customerMapper.deleteGiftsByCustId(custId);
        customerMapper.deleteProductsByCustId(custId);
        customerMapper.deleteMnpsByCustId(custId);
        // 개통상태 이력 / 상담 이력도 명시 삭제 (FK CASCADE 미설정 테이블 → orphan 방지)
        custProdStatusHistMapper.deleteHistByCustId(custId);
        custConsultMapper.deleteConsultsByCustId(custId);
        customerMapper.deleteByCustId(custId);
    }

    // ─── 첨부파일 ────────────────────────────────────────────────────────────

    /** 첨부 업로드. 해당 고객에 접근 권한이 있어야 한다(등록자/담당자/관리자·팀장). */
    @Transactional
    public List<CustFileDto> uploadFiles(Long custId, List<MultipartFile> files, CurrentUserContext currentUser) {
        if (custId == null) throw new IllegalArgumentException("고객 ID가 없습니다.");
        CustomerDto customer = customerMapper.selectCustomerById(custId);
        if (customer == null) throw new IllegalArgumentException("해당 고객 정보를 찾을 수 없습니다.");
        if (currentUser != null && !canAccessCustomer(customer, currentUser)) {
            throw new AccessForbiddenException("해당 고객에 파일을 첨부할 권한이 없습니다.");
        }
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }
        List<CustFileDto> saved = new ArrayList<>();
        for (MultipartFile mf : files) {
            if (mf == null || mf.isEmpty()) continue;
            FileStorageService.StoredFile stored = fileStorageService.store(mf);
            CustFileDto dto = new CustFileDto();
            dto.setCustId(custId);
            dto.setOriginFileName(mf.getOriginalFilename());
            dto.setStoredFileName(stored.storedFileName());
            dto.setFilePath(stored.absolutePath());
            dto.setFileSize(mf.getSize());
            dto.setContentType(mf.getContentType());
            dto.setCreatorId(currentUser != null ? currentUser.getUserId() : null);
            custFileMapper.insertFile(dto);
            saved.add(dto);
        }
        return saved;
    }

    /** 첨부 목록 조회 (접근 권한 검증) */
    public List<CustFileDto> getFiles(Long custId, CurrentUserContext currentUser) {
        if (custId == null) throw new IllegalArgumentException("고객 ID가 없습니다.");
        CustomerDto customer = customerMapper.selectCustomerById(custId);
        if (customer == null) throw new IllegalArgumentException("해당 고객 정보를 찾을 수 없습니다.");
        if (currentUser != null && !canAccessCustomer(customer, currentUser)) {
            throw new AccessForbiddenException("해당 고객의 첨부파일을 조회할 권한이 없습니다.");
        }
        return custFileMapper.selectFilesByCustId(custId);
    }

    /** 다운로드용 단건 조회 (접근 권한 검증) */
    public CustFileDto getFileForDownload(Long fileId, CurrentUserContext currentUser) {
        CustFileDto file = custFileMapper.selectFileById(fileId);
        if (file == null) throw new IllegalArgumentException("파일을 찾을 수 없습니다.");
        CustomerDto customer = customerMapper.selectCustomerById(file.getCustId());
        if (currentUser != null && customer != null && !canAccessCustomer(customer, currentUser)) {
            throw new AccessForbiddenException("해당 파일을 다운로드할 권한이 없습니다.");
        }
        return file;
    }

    /** 디스크에서 파일 실체를 Resource 로 로드 */
    public Resource loadFileResource(CustFileDto file) {
        return fileStorageService.loadAsResource(file.getFilePath());
    }

    /** 첨부 삭제 (쓰기 권한자만: 관리자/팀장). 디스크 + 메타 모두 삭제. */
    @Transactional
    public void deleteFile(Long fileId, CurrentUserContext currentUser) {
        CustFileDto file = custFileMapper.selectFileById(fileId);
        if (file == null) throw new IllegalArgumentException("파일을 찾을 수 없습니다.");
        CustomerDto customer = customerMapper.selectCustomerById(file.getCustId());
        if (currentUser != null) {
            if (!isWriter(currentUser)) {
                throw new AccessForbiddenException("첨부파일을 삭제할 권한이 없습니다. (관리자/팀장 전용)");
            }
            if (customer != null && !canAccessCustomer(customer, currentUser)) {
                throw new AccessForbiddenException("해당 파일을 삭제할 권한이 없습니다.");
            }
        }
        custFileMapper.deleteFileById(fileId);
        fileStorageService.deleteQuietly(file.getFilePath());
    }
}