package com.sys.managesys.common.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
public class NoticeDto {
    private Integer noticeId;
    private String title;
    private String content;
    private Integer viewCnt;
    private String isFixed;
    private Integer targetDeptId;
    private String fileGroupId;
    private String useYn;
    private Integer creatorId;
    private String creatorName;         // TB_USER와 조인해서 가져올 작성자 이름
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String noticeType;          // 공지사항 분류
}
