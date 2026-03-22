package com.sys.managesys.common.mapper;

import com.sys.managesys.common.dto.LoginHistoryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface LoginHistoryMapper {

    /**
     * 로그인/로그아웃 이력 저장
     */
    int insert(@Param("userId") Long userId, @Param("action") String action, @Param("ipAddress") String ipAddress, @Param("userAgent") String userAgent);

    /**
     * 특정 일시 이전의 이력 전체 조회 (덤프용)
     */
    List<LoginHistoryDto> findOlderThan(@Param("before") LocalDateTime before);

    /**
     * 특정 일시 이전의 이력 전체 삭제
     */
    int deleteOlderThan(@Param("before") LocalDateTime before);
}
