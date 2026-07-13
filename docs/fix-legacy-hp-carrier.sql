-- 레거시 통신사(HP_CARRIER) 데이터 정규화 (이슈 #6)
--
-- 증상: 고객 목록 '통신사' 컬럼에 'USIM_KT' 같은 값이 그대로 노출됨.
-- 원인: 커밋 0500a66(COMPANY_CODE→HP_CARRIER 정정) 이전에 등록된 고객은
--       TB_CUSTOMER.HP_CARRIER 컬럼에 COMPANY_CODE 그룹 값(USIM_KT, SKT, LGU 등)이 저장돼 있다.
--       목록의 통신사 컬럼은 HP_CARRIER 코드(SK/LG/KT...)를 코드명으로 조인하는데,
--       위 값들은 HP_CARRIER 그룹에 없어 조인이 실패하고 원시 코드값이 그대로 표시된다.
-- 조치: 잘못 저장된 HP_CARRIER 값을 유효한 HP_CARRIER 코드(SK/LG/KT)로 정규화한다.
--
-- 실행: mysql -h 127.0.0.1 -P 3307 -u root -p1234 managesys < docs/fix-legacy-hp-carrier.sql
-- (운영 서버는 해당 환경 접속정보로. 멱등이므로 여러 번 실행해도 안전.)

-- [사전 점검] 실제로 어떤 비정상 값이 몇 건 있는지 먼저 확인 (참고용 - 실행 전 수동 조회 권장)
-- SELECT HP_CARRIER, COUNT(*) FROM TB_CUSTOMER
--  WHERE HP_CARRIER IS NOT NULL
--    AND HP_CARRIER NOT IN ('SK','LG','KT','SK_MVNO','LG_MVNO','KT_MVNO')
--  GROUP BY HP_CARRIER ORDER BY 2 DESC;

-- SK 계열
UPDATE TB_CUSTOMER SET HP_CARRIER = 'SK'
 WHERE HP_CARRIER IN ('SKB', 'SKT', 'SKB_POP', 'USIM_SKT');

-- KT 계열  ('KT'는 COMPANY_CODE/HP_CARRIER 양쪽 값이 동일해 이미 정상이므로 제외)
UPDATE TB_CUSTOMER SET HP_CARRIER = 'KT'
 WHERE HP_CARRIER IN ('KT_SKY', 'USIM_KT', 'USIM_MKT');

-- LG 계열 (헬로비전 계열 포함)
UPDATE TB_CUSTOMER SET HP_CARRIER = 'LG'
 WHERE HP_CARRIER IN ('LGU', 'LG_SOHO', 'USIM_LGU', 'USIM_HL', 'HELLO');

-- [사후 확인] 남은 비정상 값이 있는지 재확인 (참고용)
-- SELECT HP_CARRIER, COUNT(*) FROM TB_CUSTOMER
--  WHERE HP_CARRIER IS NOT NULL
--    AND HP_CARRIER NOT IN ('SK','LG','KT','SK_MVNO','LG_MVNO','KT_MVNO')
--  GROUP BY HP_CARRIER ORDER BY 2 DESC;
