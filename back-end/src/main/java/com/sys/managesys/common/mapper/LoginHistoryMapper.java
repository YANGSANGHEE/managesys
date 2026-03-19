package com.sys.managesys.common.mapper;

import com.sys.managesys.common.dto.LoginHistoryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LoginHistoryMapper {

    /**
     * 로그인/로그아웃 이력 저장
     */
    int insert(@Param("userId") Long userId, @Param("action") String action, @Param("ipAddress") String ipAddress);
}
