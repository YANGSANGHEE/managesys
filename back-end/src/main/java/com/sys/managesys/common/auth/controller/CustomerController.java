package com.sys.managesys.common.auth.controller;

import com.sys.managesys.common.auth.dto.CurrentUserContext;
import com.sys.managesys.common.auth.service.CustomerService;
import com.sys.managesys.common.config.CustomUserDetails;
import com.sys.managesys.common.dto.*;
import com.sys.managesys.common.auth.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
// CORS 는 SecurityConfig.corsConfigurationSource() 의 allow-list 로 일원화한다.
// (컨트롤러 레벨 @CrossOrigin("*") 은 allow-list 를 무력화하므로 제거)
public class CustomerController {

    private final CustomerService customerService;
    private final UserManagementService userManagementService;

    private CurrentUserContext getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            return null;
        }
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        UserDto u = details.getUserDto();
        Long deptId = u != null ? u.getDeptId() : null;
        return CurrentUserContext.builder()
                .userId(details.getUserId())
                .loginId(u != null ? u.getLoginId() : null)
                .userRole(details.getRoleCode())
                .deptId(deptId)
                .build();
    }

    @PostMapping("/list")
    public List<CustomerDto> getCustomerList(@RequestBody CustomerDto searchDto) {
        CurrentUserContext user = getCurrentUser();
        if (user != null) {
            searchDto.setFilterUserRole(user.getUserRole());
            searchDto.setFilterUserId(user.getUserId());
            searchDto.setFilterDeptId(user.getDeptId());
        }
        return customerService.findCustomers(searchDto);
    }

    @GetMapping("/users/options")
    public List<CounselorOptionDto> getCounselorOptions() {
        UserDto criteria = new UserDto();
        criteria.setUseYn("Y");
        List<UserDto> users = userManagementService.findAllUsers(criteria);
        return users.stream()
                .map(u -> CounselorOptionDto.builder()
                        .userId(u.getUserId())
                        .userName(u.getUserName())
                        .deptId(u.getDeptId())
                        .build())
                .collect(Collectors.toList());
    }

    @GetMapping("/detail")
    public CustomerDetailResponse getCustomerDetail(@RequestParam Long custId) {
        return customerService.getCustomerDetail(custId, getCurrentUser());
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerCustomer(@RequestBody CustomerRegisterRequest request) {
        CurrentUserContext user = getCurrentUser();
        if (user != null && request.getCustomer() != null) {
            // 등록자(creatorId)는 클라이언트 입력을 신뢰하지 않고 항상 서버에서 현재 사용자로 강제 (명의 위조 방지)
            request.getCustomer().setCreatorId(user.getUserId());
            // 담당자(assignedUserId) 미지정 시 등록자 본인으로 기본 배정
            if (request.getCustomer().getAssignedUserId() == null) {
                request.getCustomer().setAssignedUserId(user.getUserId());
            }
        }
        Long custId = customerService.register(request);
        // 신규 등록 직후 첨부파일을 올릴 수 있도록 custId 반환
        return ResponseEntity.ok(Map.of("custId", custId));
    }

    @PutMapping("/update")
    public void updateCustomer(@RequestBody CustomerRegisterRequest request) {
        customerService.update(request, getCurrentUser());
    }


    @PatchMapping("/{custId}/quick-update")
    public ResponseEntity<Void> quickUpdate(@PathVariable Long custId, @RequestBody Map<String, String> body) {
        String field = body.get("field");
        String value = body.get("value");
        String prodIdStr = body.get("prodId");
        Long prodId = (prodIdStr == null || prodIdStr.isBlank()) ? null : Long.valueOf(prodIdStr);
        customerService.quickUpdate(custId, prodId, field, value, getCurrentUser());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{custId}/status-hist")
    public List<CustProdStatusHistDto> getStatusHist(@PathVariable Long custId) {
        return customerService.getStatusHist(custId, getCurrentUser());
    }

    @GetMapping("/{custId}/consults")
    public List<CustConsultDto> getConsults(@PathVariable Long custId) {
        return customerService.getConsults(custId, getCurrentUser());
    }

    @PostMapping("/{custId}/consults")
    public ResponseEntity<Void> addConsult(@PathVariable Long custId, @RequestBody CustConsultDto dto) {
        CurrentUserContext user = getCurrentUser();
        dto.setCustId(custId);
        if (user != null) dto.setCreatorId(user.getUserId());
        customerService.addConsult(dto, user);
        return ResponseEntity.ok().build();
    }

    /** 고객 삭제 (TB_CUSTOMER 삭제 시 CASCADE로 TB_CUST_PRODUCT, TB_CUST_GIFT, TB_CUST_PAYMENT, TB_CUST_MNP 연쇄 삭제) */
    @DeleteMapping("/{custId}")
    public void deleteCustomer(@PathVariable Long custId) {
        customerService.deleteCustomer(custId, getCurrentUser());
    }

    // ─── 첨부파일 ────────────────────────────────────────────────────────────

    /** 첨부 업로드 (multipart). 해당 고객 접근 권한자만. */
    @PostMapping("/{custId}/files")
    public ResponseEntity<List<CustFileDto>> uploadFiles(@PathVariable Long custId,
                                                         @RequestParam("files") List<MultipartFile> files) {
        return ResponseEntity.ok(customerService.uploadFiles(custId, files, getCurrentUser()));
    }

    /** 첨부 목록 */
    @GetMapping("/{custId}/files")
    public List<CustFileDto> getFiles(@PathVariable Long custId) {
        return customerService.getFiles(custId, getCurrentUser());
    }

    /** 첨부 다운로드 */
    @GetMapping("/files/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        CustFileDto file = customerService.getFileForDownload(fileId, getCurrentUser());
        Resource resource = customerService.loadFileResource(file);
        String encodedName = URLEncoder.encode(
                file.getOriginFileName() != null ? file.getOriginFileName() : "download",
                StandardCharsets.UTF_8).replace("+", "%20");
        String contentType = file.getContentType() != null ? file.getContentType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedName + "\"; filename*=UTF-8''" + encodedName)
                .body(resource);
    }

    /** 첨부 삭제 (관리자/팀장) */
    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long fileId) {
        customerService.deleteFile(fileId, getCurrentUser());
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleValidation(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
}