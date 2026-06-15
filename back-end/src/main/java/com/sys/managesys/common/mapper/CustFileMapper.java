package com.sys.managesys.common.mapper;

import com.sys.managesys.common.dto.CustFileDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CustFileMapper {

    /** 첨부파일 메타 등록 (insert 후 fileId 채워짐) */
    int insertFile(CustFileDto file);

    /** 특정 고객의 첨부파일 목록 (USE_YN='Y') */
    List<CustFileDto> selectFilesByCustId(@Param("custId") Long custId);

    /** 단건 조회 (다운로드/삭제용) */
    CustFileDto selectFileById(@Param("fileId") Long fileId);

    /** 단건 물리 삭제 */
    int deleteFileById(@Param("fileId") Long fileId);

    /** 고객의 전체 첨부 물리 삭제 (고객 삭제 시) */
    int deleteFilesByCustId(@Param("custId") Long custId);
}
