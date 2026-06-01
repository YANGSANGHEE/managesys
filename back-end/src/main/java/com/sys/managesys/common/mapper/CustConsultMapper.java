package com.sys.managesys.common.mapper;

import com.sys.managesys.common.dto.CustConsultDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CustConsultMapper {
    List<CustConsultDto> selectConsultsByCustId(@Param("custId") Long custId);
    int insertConsult(CustConsultDto dto);
}
