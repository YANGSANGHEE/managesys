-- =============================================================================
-- 고객 관리 시스템 DB 무결성 검증용 SQL
-- CRUD(등록/조회/수정/삭제) 후 연관 테이블 데이터 정합성 확인
-- 사용법: MariaDB/MySQL 클라이언트에서 블록별로 실행. 변수 사용 시 먼저 SET 실행.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. 신규 등록 후 검증: 특정 고객(CUST_ID) 기준 전 테이블 데이터 존재 여부
--    (아래 SET에서 @custId를 실제 등록된 CUST_ID로 바꾼 뒤 1번 블록 실행)
-- -----------------------------------------------------------------------------
SET @custId = 1;  -- 예: 방금 등록한 고객 ID (실제 검증 시 값 변경)

-- TB_CUSTOMER: 기본 정보, CUST_KIND, JOIN_DATE, RECEIPT_DT, INSTALL_SCHED_DT 등
SELECT CUST_ID, CUST_NAME, CUST_KIND, CUST_TYPE, STATUS,
       JOIN_DATE, RECEIPT_DT, INSTALL_SCHED_DT, CANCELL_DT,
       USE_YN, CREATOR_ID, CREATED_AT
FROM TB_CUSTOMER
WHERE CUST_ID = @custId;

-- TB_CUST_PRODUCT: 상품 정보, 개통일/해지일, CUST_ID FK
SELECT PROD_ID, CUST_ID, COMP_NAME, PROD_GB, PROD_NAME, SUBSCRIPTION_NO,
       OPEN_DATE, CANCEL_DATE, CONTRACT_PERIOD, USE_YN
FROM TB_CUST_PRODUCT
WHERE CUST_ID = @custId
ORDER BY PROD_ID;

-- TB_CUST_PAYMENT: 납부 정보, CUST_ID FK
SELECT CUST_ID, PAY_METHOD, BILL_TYPE, DISCOUNT_TYPE, BANK_CARD_NAME,
       ACCOUNT_CARD_NO, ACCOUNT_HOLDER, RELATION_WITH_CUST, USE_YN
FROM TB_CUST_PAYMENT
WHERE CUST_ID = @custId;

-- TB_CUST_GIFT: 사은품(GENERAL/HEAD), CUST_ID FK
SELECT GIFT_ID, CUST_ID, GIFT_GB, GIFT_NAME, GIFT_AMOUNT, ADD_DEPOSIT_AMOUNT,
       PAY_SCHED_DATE, PAY_DONE_DATE, PAY_SOURCE, USE_YN
FROM TB_CUST_GIFT
WHERE CUST_ID = @custId
ORDER BY GIFT_GB, GIFT_ID;

-- TB_CUST_MNP: 번호이동, CUST_ID FK
SELECT MNP_ID, CUST_ID, IS_SAME_AS_CUST, MNP_TEL_NO, OWNER_NAME,
       ISSUE_DATE, PREV_CARRIER, USE_YN
FROM TB_CUST_MNP
WHERE CUST_ID = @custId;


-- -----------------------------------------------------------------------------
-- 2. 수정 후 검증: 동일 CUST_ID로 위 1번 쿼리 재실행하여 값이 덮어씌워졌는지 확인
--    (JOIN_DATE, RECEIPT_DT, 상품 OPEN_DATE/CANCEL_DATE 등 변경값 확인)
-- -----------------------------------------------------------------------------
-- 수정 전/후 비교용: 최근 수정된 고객의 UPDATED_AT 확인
SELECT CUST_ID, CUST_NAME, UPDATED_AT, JOIN_DATE, RECEIPT_DT
FROM TB_CUSTOMER
WHERE CUST_KIND = 'INDIVIDUAL' AND USE_YN = 'Y'
ORDER BY UPDATED_AT DESC
LIMIT 5;


-- -----------------------------------------------------------------------------
-- 3. 삭제 후 검증: 고아 데이터(Orphan) 없음 확인
--    - 삭제한 CUST_ID가 TB_CUSTOMER에 없어야 함
--    - 해당 CUST_ID를 참조하는 자식 테이블 행이 0건이어야 함
-- -----------------------------------------------------------------------------
-- 3-1. 특정 CUST_ID 삭제 후 자식 테이블에 잔존 행이 있는지 확인 (결과 0건이어야 정상)
SET @deletedCustId = 999;  -- 삭제한 고객 ID (실제 검증 시 값 변경)

SELECT 'TB_CUST_PAYMENT' AS tbl, COUNT(*) AS cnt FROM TB_CUST_PAYMENT WHERE CUST_ID = @deletedCustId
UNION ALL
SELECT 'TB_CUST_GIFT',    COUNT(*) FROM TB_CUST_GIFT    WHERE CUST_ID = @deletedCustId
UNION ALL
SELECT 'TB_CUST_PRODUCT', COUNT(*) FROM TB_CUST_PRODUCT WHERE CUST_ID = @deletedCustId
UNION ALL
SELECT 'TB_CUST_MNP',     COUNT(*) FROM TB_CUST_MNP     WHERE CUST_ID = @deletedCustId;

-- 3-2. 전 테이블 고아 데이터 검사: TB_CUSTOMER에 없는 CUST_ID를 참조하는 자식 행 (결과 0건이어야 정상)
SELECT 'TB_CUST_PAYMENT' AS tbl, P.CUST_ID
FROM TB_CUST_PAYMENT P
LEFT JOIN TB_CUSTOMER C ON C.CUST_ID = P.CUST_ID
WHERE C.CUST_ID IS NULL
UNION ALL
SELECT 'TB_CUST_GIFT', G.CUST_ID
FROM TB_CUST_GIFT G
LEFT JOIN TB_CUSTOMER C ON C.CUST_ID = G.CUST_ID
WHERE C.CUST_ID IS NULL
UNION ALL
SELECT 'TB_CUST_PRODUCT', P.CUST_ID
FROM TB_CUST_PRODUCT P
LEFT JOIN TB_CUSTOMER C ON C.CUST_ID = P.CUST_ID
WHERE C.CUST_ID IS NULL
UNION ALL
SELECT 'TB_CUST_MNP', M.CUST_ID
FROM TB_CUST_MNP M
LEFT JOIN TB_CUSTOMER C ON C.CUST_ID = M.CUST_ID
WHERE C.CUST_ID IS NULL;


-- -----------------------------------------------------------------------------
-- 4. 개인 고객 리스트 조건 검증: CUST_KIND = 'INDIVIDUAL', USE_YN = 'Y'
-- -----------------------------------------------------------------------------
SELECT CUST_ID, CUST_NAME, CUST_KIND, USE_YN
FROM TB_CUSTOMER
WHERE CUST_KIND = 'INDIVIDUAL' AND USE_YN = 'Y'
ORDER BY CREATED_AT DESC
LIMIT 20;


-- -----------------------------------------------------------------------------
-- 5. FK 연결 요약: 고객별 자식 테이블 건수 (등록/수정 후 건수 확인용)
-- -----------------------------------------------------------------------------
SELECT C.CUST_ID, C.CUST_NAME,
       (SELECT COUNT(*) FROM TB_CUST_PAYMENT  WHERE CUST_ID = C.CUST_ID) AS pay_cnt,
       (SELECT COUNT(*) FROM TB_CUST_GIFT     WHERE CUST_ID = C.CUST_ID) AS gift_cnt,
       (SELECT COUNT(*) FROM TB_CUST_PRODUCT  WHERE CUST_ID = C.CUST_ID) AS prod_cnt,
       (SELECT COUNT(*) FROM TB_CUST_MNP      WHERE CUST_ID = C.CUST_ID) AS mnp_cnt
FROM TB_CUSTOMER C
WHERE C.CUST_KIND = 'INDIVIDUAL' AND C.USE_YN = 'Y'
ORDER BY C.CREATED_AT DESC
LIMIT 20;
