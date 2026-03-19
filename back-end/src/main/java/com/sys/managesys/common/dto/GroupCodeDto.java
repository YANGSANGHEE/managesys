package com.sys.managesys.common.dto;

import lombok.*;

/**
 * 그룹코드 CRUD용 DTO (TB_GROUP_CODE)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupCodeDto {
    private String groupCode;
    private String groupName;
    private String useYn;
    private String remark;
}
