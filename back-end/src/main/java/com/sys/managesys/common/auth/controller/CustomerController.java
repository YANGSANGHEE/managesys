package com.sys.managesys.common.auth.controller;

import com.sys.managesys.common.auth.dto.CurrentUserContext;
import com.sys.managesys.common.auth.service.CustomerService;
import com.sys.managesys.common.config.CustomUserDetails;
import com.sys.managesys.common.dto.*;
import com.sys.managesys.common.auth.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {
        RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
        RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS
})
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
    public void registerCustomer(@RequestBody CustomerRegisterRequest request) {
        CurrentUserContext user = getCurrentUser();
        if (user != null && request.getCustomer() != null && request.getCustomer().getCreatorId() == null) {
            request.getCustomer().setCreatorId(user.getUserId());
        }
        customerService.register(request);
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
        customerService.quickUpdate(custId, prodId, field, value);
        return ResponseEntity.ok().build();
    }

    /** 고객 삭제 (TB_CUSTOMER 삭제 시 CASCADE로 TB_CUST_PRODUCT, TB_CUST_GIFT, TB_CUST_PAYMENT, TB_CUST_MNP 연쇄 삭제) */
    @DeleteMapping("/{custId}")
    public void deleteCustomer(@PathVariable Long custId) {
        customerService.deleteCustomer(custId, getCurrentUser());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleValidation(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
}