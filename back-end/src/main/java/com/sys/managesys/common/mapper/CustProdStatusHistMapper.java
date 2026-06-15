package com.sys.managesys.common.mapper;

import com.sys.managesys.common.dto.CustProdStatusHistDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CustProdStatusHistMapper {
    List<CustProdStatusHistDto> selectHistByCustId(@Param("custId") Long custId);
    int insertHist(CustProdStatusHistDto dto);
    /** 고객 삭제 시 개통상태 이력 연쇄 삭제 (orphan 방지) */
    int deleteHistByCustId(@Param("custId") Long custId);
}
