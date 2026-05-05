package com.sys.managesys.common.mapper;

import com.sys.managesys.common.dto.CommonCodeDto;
import com.sys.managesys.common.dto.GroupCodeDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface CommonCodeMapper {

    /** 드롭다운용: USE_YN='Y' 목록만 */
    List<CommonCodeDto> selectByGroupCode(@Param("groupCode") String groupCode);

    // ─── 그룹코드 CRUD ───────────────────────────────────────────────────────
    List<GroupCodeDto> selectAllGroups(@Param("groupName") String groupName);
    int insertGroupCode(GroupCodeDto dto);
    int updateGroupCode(GroupCodeDto dto);
    int deleteGroupCode(@Param("groupCode") String groupCode);
    int countCodesByGroupCode(@Param("groupCode") String groupCode);

    // ─── 상세코드 CRUD ───────────────────────────────────────────────────────
    List<CommonCodeDto> selectAllByGroupCode(@Param("groupCode") String groupCode);
    int insertCode(CommonCodeDto dto);
    int updateCode(CommonCodeDto dto);
    int deleteCode(@Param("groupCode") String groupCode, @Param("codeValue") String codeValue);

    /** 상품 cascade 매핑 전체 행 (TB_PROD_CASCADE_MAP) */
    List<Map<String, Object>> selectProdCascadeRows();
}
