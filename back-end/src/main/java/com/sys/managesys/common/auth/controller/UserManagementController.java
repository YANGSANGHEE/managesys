package com.sys.managesys.common.auth.controller;

import com.sys.managesys.common.auth.service.UserManagementService;
import com.sys.managesys.common.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserManagementService userService;

    // 직원 목록 조회 (POST)
    @PostMapping("/list")
    public List<UserDto> getUserList(@RequestBody UserDto searchDto) {
        return userService.findAllUsers(searchDto);
    }

    // 직원 등록 (POST)
    @PostMapping("/register")
    public void registerUser(@RequestBody UserDto userDto) {
        userService.register(userDto);
    }

    // 직원 수정 (POST)
    @PostMapping("/modify")
    public void updateUser(@RequestBody UserDto userDto) {
        userService.modify(userDto);
    }

    // 직원 삭제 (POST)
    @PostMapping("/remove")
    public void deleteUser(@RequestBody UserDto userDto) {
        // ID만 담긴 객체를 받아 처리
        userService.remove(userDto.getUserId());
    }

    @PostMapping("/check-id")
    public Map<String, Boolean> checkId(@RequestBody Map<String, String> params) {
        boolean isDuplicate = userService.isIdDuplicate(params.get("loginId"));
        return Collections.singletonMap("isDuplicate", isDuplicate);
    }

    // 부서 목록 조회 (팝업용)
    @PostMapping("/dept/list")
    public List<Map<String, Object>> getDeptList(@RequestBody Map<String, String> params) {
        return userService.findDepartments(params.get("deptName"));
    }


}