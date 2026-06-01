package com.sys.managesys.common.mapper;

import com.sys.managesys.common.dto.CustProdStatusHistDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CustProdStatusHistMapper {
    List<CustProdStatusHistDto> selectHistByCustId(@Param("custId") Long custId);
    int insertHist(CustProdStatusHistDto dto);
}
