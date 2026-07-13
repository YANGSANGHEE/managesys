-- CONSULT_TYPE(상담구분/회신 구분) 공통코드 추가
--  - REPLY_ANSWER(회신답변): 하부점(담당자)이 상부점의 '회신요청'에 답하는 구분 (이슈 #1)
--  - VOC_ISSUE(VOC발행), SR_ISSUE(SR발행): 상부점이 하부점에 내리는 회신 항목 (이슈 #2)
-- 프론트(IndividualCustomerManagement.vue)의 상담구분 드롭다운은 이 코드를 그대로 로드하므로,
-- INSERT만으로 드롭다운에 자동 노출된다. 배지/행 색상은 프론트 코드에서 별도 처리.
--
-- 실행: mysql -h 127.0.0.1 -P 3307 -u root -p1234 managesys < docs/insert-consult-type-codes.sql
-- (운영 서버는 해당 환경의 접속정보로 실행. 멱등이므로 여러 번 실행해도 안전.)

INSERT IGNORE INTO TB_GROUP_CODE (GROUP_CODE, GROUP_NAME, USE_YN) VALUES ('CONSULT_TYPE', '상담구분', 'Y');

INSERT INTO TB_COMMON_CODE (GROUP_CODE, CODE_VALUE, CODE_NAME, SORT_ORDER, USE_YN, REMARK) VALUES
  ('CONSULT_TYPE', 'REPLY_ANSWER', '회신답변', 4, 'Y', '담당자가 관리자의 회신요청에 답하는 경우'),
  ('CONSULT_TYPE', 'VOC_ISSUE',    'VOC발행',  5, 'Y', '상부점이 하부점에 VOC 발행'),
  ('CONSULT_TYPE', 'SR_ISSUE',     'SR발행',   6, 'Y', '상부점이 하부점에 SR 발행')
ON DUPLICATE KEY UPDATE
  CODE_NAME = VALUES(CODE_NAME),
  SORT_ORDER = VALUES(SORT_ORDER),
  USE_YN = VALUES(USE_YN),
  REMARK = VALUES(REMARK),
  UPDATED_AT = CURRENT_TIMESTAMP;
