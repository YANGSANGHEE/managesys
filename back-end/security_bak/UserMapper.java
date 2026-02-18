package com.sys.managesys.common.mapper;

import com.sys.managesys.common.dto.UserDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    // 로그인 시 (LOGIN_ID 기준)
    UserDto findByLoginId(@Param("loginId") String loginId);

    // JWT 인증 시 (USER_ID 기준)
    UserDto findByUserId(@Param("userId") Long userId);
}
