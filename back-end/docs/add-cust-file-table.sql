-- ============================================================
-- 개인/협력점 고객 첨부파일 테이블 (TB_CUST_FILE)
-- 작성: 2026-06-15
-- 설명: 고객 첨부파일(이미지/PDF) 메타정보 저장. 파일 실체는 디스크에 저장하고
--       이 테이블에는 경로/원본명 등 메타데이터만 보관한다.
--       TB_CUSTOMER 삭제 시 ON DELETE CASCADE 로 함께 삭제된다.
-- ============================================================
CREATE TABLE IF NOT EXISTS TB_CUST_FILE (
  FILE_ID INT NOT NULL AUTO_INCREMENT COMMENT '첨부파일 ID',
  CUST_ID INT NOT NULL COMMENT '고객 ID',
  ORIGIN_FILE_NAME VARCHAR(255) NOT NULL COMMENT '원본 파일명',
  STORED_FILE_NAME VARCHAR(255) NOT NULL COMMENT '저장 파일명(UUID)',
  FILE_PATH VARCHAR(500) NOT NULL COMMENT '저장 경로(절대)',
  FILE_SIZE BIGINT COMMENT '파일 크기(byte)',
  CONTENT_TYPE VARCHAR(100) COMMENT 'MIME 타입',
  CREATOR_ID INT COMMENT '업로드한 사용자 ID',
  CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '업로드 일시',
  USE_YN CHAR(1) DEFAULT 'Y' COMMENT '사용여부',
  PRIMARY KEY (FILE_ID),
  KEY IDX_CUST_FILE_CUST (CUST_ID),
  CONSTRAINT FK_CUST_FILE_CUST FOREIGN KEY (CUST_ID) REFERENCES TB_CUSTOMER(CUST_ID) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='개인/협력점 고객 첨부파일';
