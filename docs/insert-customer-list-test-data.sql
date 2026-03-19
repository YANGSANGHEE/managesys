-- 고객 등록 리스트 확인용 테스트 INSERT
-- 리스트는 TB_CUSTOMER만 조회하므로 고객 마스터만 넣으면 됨.
-- 실행: mysql -h 127.0.0.1 -P 3307 -u root -p1234 managesys < docs/insert-customer-list-test-data.sql

-- CREATOR_ID = 1 (TB_USER에 있는 admin) 사용 시 등록자명이 표시됨.

INSERT INTO TB_CUSTOMER (
  STATUS, CUST_NAME, REP_NAME, CUST_TYPE, CUST_KIND,
  ZIP_CODE, ADDR, ADDR_DETAIL, TEL_NO, HP_NO, HP_CARRIER, EMAIL,
  COUNSELOR_NAME, REMARK, USE_YN, CREATOR_ID
) VALUES
('접수', '김리스트', NULL, 'PERS', 'INDIVIDUAL',
 '48058', '부산 해운대구 센텀중앙로 79', '101동 1001호', '051-123-4567', '010-1111-2222', 'SKT', 'kim.list@test.com',
 '양상희', '리스트 테스트1', 'Y', 1),

('개통완료', '이등록', NULL, 'PERS', 'INDIVIDUAL',
 '48059', '부산 수영구 광안해변로 123', '202동 501호', '051-234-5678', '010-2222-3333', 'KT', 'lee.reg@test.com',
 '양상희', NULL, 'Y', 1),

('접수', '박고객', NULL, 'PERS', 'INDIVIDUAL',
 '48060', '부산 남구 용호로 45', '303동 202호', NULL, '010-3333-4444', 'LGU', NULL,
 '최고관리자', '테스트 데이터', 'Y', 1),

('접수', '최확인', NULL, 'PERS', 'INDIVIDUAL',
 '48061', '부산 동래구 충렬대로 100', NULL, '051-345-6789', '010-4444-5555', 'SKB', 'choi@test.com',
 NULL, NULL, 'Y', 1),

('개통완료', '정테스트', NULL, 'PERS', 'INDIVIDUAL',
 '48062', '부산 금정구 부산대학로 63', '단독주택', '051-456-7890', '010-5555-6666', 'SKT', 'jung@test.com',
 '양상희', '리스트 확인용', 'Y', 1),

('취소', '한샘플', NULL, 'PERS', 'INDIVIDUAL',
 NULL, '서울 강남구 테헤란로 152', '오피스 7층', '02-1234-5678', '010-6666-7777', 'KT', 'han@test.com',
 NULL, '취소 샘플', 'Y', 1),

('접수', '오디스플레이', NULL, 'PERS', 'INDIVIDUAL',
 '48063', '부산 연제구 연산동 1000', '아파트 405호', NULL, '010-7777-8888', 'LGU', 'oh@test.com',
 '최고관리자', NULL, 'Y', 1),

('개통완료', '강리스트', NULL, 'BIZP', 'INDIVIDUAL',
 '48064', '부산 사하구 낙동남로 500', '1층', '051-567-8901', '010-8888-9999', 'SKB', 'kang@test.com',
 '양상희', '개인사업자 샘플', 'Y', 1);
