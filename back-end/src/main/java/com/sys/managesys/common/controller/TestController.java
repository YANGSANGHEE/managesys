package com.sys.managesys.common.controller;

import com.sys.managesys.common.dto.UserDto;
import com.sys.managesys.common.service.TestService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value="/api/get")
public class TestController {
    @Resource(name = "testService")
    private TestService testService;

    /* 문자열 리턴 확인용 */
    @GetMapping("/hello")
    public String hello(){
        return "helloworld";
    }

    /* API 리턴 확인용 */
    @GetMapping("/user")
    public UserDto getUser(){
        return testService.getUser();
    }
}
