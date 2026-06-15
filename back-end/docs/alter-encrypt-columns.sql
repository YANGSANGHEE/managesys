-- ============================================================
-- 개인정보 암호화(AES-256-GCM) 적용을 위한 컬럼 길이 확장
-- 작성: 2026-06-15
-- 설명: 평문 대비 암호문(enc:v1:Base64(IV||CT||TAG))이 길어지므로(예: 주민번호 14자 → 약 63자)
--       대상 컬럼을 VARCHAR(255)로 보장한다. 이미 255인 컬럼은 변경 없이 무해.
--       ⚠ 이 DDL 적용 후, 애플리케이션을 --app.encryption.migrate-on-startup=true 로 1회 기동하면
--         기존 평문 데이터가 일괄 암호화된다(멱등). 적용 후 플래그는 false 로 되돌릴 것.
-- ============================================================
ALTER TABLE TB_CUSTOMER     MODIFY SSN_ENC          VARCHAR(255);
ALTER TABLE TB_CUSTOMER     MODIFY FOREIGNER_REG_NO VARCHAR(255);
ALTER TABLE TB_CUST_PAYMENT MODIFY ACCOUNT_CARD_NO  VARCHAR(255);
ALTER TABLE TB_CUST_PAYMENT MODIFY HOLDER_SSN_ENC   VARCHAR(255);
ALTER TABLE TB_CUST_GIFT    MODIFY HOLDER_SSN_ENC   VARCHAR(255);
ALTER TABLE TB_CUST_GIFT    MODIFY ACCOUNT_NO       VARCHAR(255);
ALTER TABLE TB_CUST_MNP     MODIFY OWNER_SSN_ENC    VARCHAR(255);
