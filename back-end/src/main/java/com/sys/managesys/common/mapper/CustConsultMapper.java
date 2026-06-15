package com.sys.managesys.common.mapper;

import com.sys.managesys.common.dto.CustConsultDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CustConsultMapper {
    List<CustConsultDto> selectConsultsByCustId(@Param("custId") Long custId);
    int insertConsult(CustConsultDto dto);
    /** 고객 삭제 시 상담 이력 연쇄 삭제 (orphan 방지) */
    int deleteConsultsByCustId(@Param("custId") Long custId);
}
