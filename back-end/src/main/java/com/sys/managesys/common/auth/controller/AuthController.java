/* 권한 컨트롤러
*  로그인
* */

package com.sys.managesys.common.auth.controller;

import com.sys.managesys.common.auth.service.AuthService;
import com.sys.managesys.common.dto.LoginRequest;
import com.sys.managesys.common.dto.LoginResponse;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {
    @Resource(name = "authService")
    private final AuthService authService;

//    @PostMapping("/login")

}
