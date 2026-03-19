-- TB_CUSTOMER에 접수일(RECEIPT_DT) 컬럼 추가
-- 컬럼이 이미 있으면 "Duplicate column name" 오류가 나므로, 한 번만 실행하세요.

ALTER TABLE TB_CUSTOMER
  ADD COLUMN RECEIPT_DT DATE DEFAULT NULL COMMENT '접수일';
