-- 권한/부서별 개인 고객 리스트 필터링 테스트용 데이터
-- 1) 부서: TB_DEPARTMENT (이미 있으면 스킵)
-- 2) 사용자: ADMIN 1명, MANAGER 1명, MEMBER 2명 (비밀번호는 BCrypt, 예: password1)
-- 3) 개인 고객: CUST_KIND='INDIVIDUAL', DEPT_ID/ASSIGNED_USER_ID 조합으로 권한별 조회 결과 검증

-- 비밀번호 'password1' BCrypt 해시 (앱의 /api/auth/generate-hash?password=password1 로 생성 가능)
SET @pw = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy';

-- 부서 (이미 있으면 무시)
INSERT IGNORE INTO TB_DEPARTMENT (DEPT_ID, DEPT_NAME, PARENT_ID, USE_YN, CREATED_AT, UPDATED_AT) VALUES
(1, '본사', NULL, 'Y', NOW(), NOW()),
(2, '영업1팀', NULL, 'Y', NOW(), NOW()),
(3, '영업2팀', NULL, 'Y', NOW(), NOW());

-- 사용자: admin(ADMIN), manager1(MANAGER/영업1팀), member1(MEMBER/영업1팀), member2(MEMBER/영업2팀)
INSERT INTO TB_USER (LOGIN_ID, PASSWORD, USER_NAME, DEPT_ID, USER_ROLE, IS_LEADER, USE_YN, CREATED_AT) VALUES
('admin', @pw, '관리자', 1, 'ADMIN', 0, 'Y', NOW()),
('manager1', @pw, '팀장김', 2, 'MANAGER', 1, 'Y', NOW()),
('member1', @pw, '사원이', 2, 'MEMBER', 0, 'Y', NOW()),
('member2', @pw, '사원박', 3, 'MEMBER', 0, 'Y', NOW())
ON DUPLICATE KEY UPDATE USER_NAME = VALUES(USER_NAME), DEPT_ID = VALUES(DEPT_ID), USER_ROLE = VALUES(USER_ROLE);

-- 2) 사용자 ID 변수 설정 (위 사용자 INSERT 후 실행)
SELECT USER_ID INTO @uid_admin FROM TB_USER WHERE LOGIN_ID = 'admin' LIMIT 1;
SELECT USER_ID INTO @uid_m1 FROM TB_USER WHERE LOGIN_ID = 'member1' LIMIT 1;
SELECT USER_ID INTO @uid_m2 FROM TB_USER WHERE LOGIN_ID = 'member2' LIMIT 1;

-- 3) 개인 고객 (CUST_KIND = 'INDIVIDUAL' 만 리스트에 조회됨)
-- 기대: admin → 5건, manager1 → 3건(영업1팀), member1 → 2건(본인 배정), member2 → 2건(본인 배정)
INSERT INTO TB_CUSTOMER (CUST_NAME, STATUS, CUST_TYPE, CUST_KIND, DEPT_ID, ASSIGNED_USER_ID, CREATOR_ID, USE_YN, CREATED_AT, UPDATED_AT) VALUES
('개인고객-A(영업1팀-member1)', 'RECEIPT', 'PERS', 'INDIVIDUAL', 2, @uid_m1, @uid_admin, 'Y', NOW(), NOW()),
('개인고객-B(영업1팀-member1)', 'COMPLETED', 'PERS', 'INDIVIDUAL', 2, @uid_m1, @uid_admin, 'Y', NOW(), NOW()),
('개인고객-C(영업1팀-담당무)', 'RECEIPT', 'PERS', 'INDIVIDUAL', 2, NULL, @uid_admin, 'Y', NOW(), NOW()),
('개인고객-D(영업2팀-member2)', 'RECEIPT', 'PERS', 'INDIVIDUAL', 3, @uid_m2, @uid_admin, 'Y', NOW(), NOW()),
('개인고객-E(영업2팀-member2)', 'COMPLETED', 'PERS', 'INDIVIDUAL', 3, @uid_m2, @uid_admin, 'Y', NOW(), NOW());
