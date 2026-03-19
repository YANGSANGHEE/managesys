package com.sys.managesys.common.dto;

import lombok.*;

/**
 * 공통코드 조회용 DTO (TB_COMMON_CODE)
 * 프론트에서 select option의 value=codeValue, text=codeName 으로 사용
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommonCodeDto {
    private String groupCode;
    private String codeValue;
    private String codeName;
    private Integer sortOrder;
    private String useYn;
    private String remark;
}
