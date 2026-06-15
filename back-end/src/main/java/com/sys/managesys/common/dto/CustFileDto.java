package com.sys.managesys.common.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * 고객 첨부파일 메타 DTO (TB_CUST_FILE)
 * 파일 실체는 디스크에 저장하고, 본 DTO/테이블에는 메타정보만 보관한다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustFileDto {
    private Long fileId;
    private Long custId;
    private String originFileName;   // 원본 파일명 (프론트 표시/다운로드명)
    private String storedFileName;   // 디스크 저장 파일명 (UUID)
    private String filePath;         // 디스크 절대경로
    private Long fileSize;           // byte
    private String contentType;      // MIME
    private Long creatorId;
    private LocalDateTime createdAt;
    private String useYn;
}
