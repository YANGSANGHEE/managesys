package com.sys.managesys.common.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeDto {
    private Long noticeId;
    private String noticeType;
    private String title;
    private String content;
    private Integer viewCnt;
    private String isFixed;
    private String useYn;
    private Long creatorId;
    private String creatorName;
    private String createdAt;
}
