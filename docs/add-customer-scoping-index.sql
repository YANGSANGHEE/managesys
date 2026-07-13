-- 팀장 고객목록 스코핑 쿼리 성능 보강 (Codex 리뷰 LOW)
--
-- 대상: CustomerMapper.selectCustomerList 의 MANAGER(팀장) 분기.
--       팀장 목록은 "등록자(CREATOR_ID)의 현재 부서가 팀장 부서"인 고객도 포함하는데,
--       TB_CUSTOMER.CREATOR_ID 는 FK 가 아니라 인덱스가 없다(DEPT_ID/ASSIGNED_USER_ID 는 FK 인덱스 존재).
--       고객 수가 늘면 이 조건이 풀스캔을 유발할 수 있어 인덱스를 추가한다.
--
-- 실행: mysql -h 127.0.0.1 -P 3307 -u root -p1234 managesys < docs/add-customer-scoping-index.sql
-- (운영은 해당 환경 접속정보로. IF NOT EXISTS 라 여러 번 실행해도 안전.)

CREATE INDEX IF NOT EXISTS IDX_CUST_CREATOR ON TB_CUSTOMER (CREATOR_ID);
