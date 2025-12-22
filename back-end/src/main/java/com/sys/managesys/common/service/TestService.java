package com.sys.managesys.common.service;

import com.sys.managesys.common.dto.UserDto;
import org.springframework.stereotype.Service;

@Service("testService")
public class TestService {
    public UserDto getUser(){
        return new UserDto(1L,"이주영",20);
    }
}
