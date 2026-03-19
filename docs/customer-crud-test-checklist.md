# 고객 관리 시스템 전 기능 테스트 및 DB 무결성 체크리스트

## 1. 신규 등록 (Create)
- [ ] 고객 정보·납부·사은품·본사 사은품·상품·번호이동 전체 입력 후 저장
- [ ] 저장 성공 시 리스트(AG Grid)에 즉시 반영되는지 확인
- [ ] **DB 검증**: `docs/customer-db-integrity-check.sql` 1번 블록 실행  
  → TB_CUSTOMER, TB_CUST_PRODUCT, TB_CUST_PAYMENT, TB_CUST_GIFT, TB_CUST_MNP에 해당 CUST_ID로 데이터 존재 확인

## 2. 유효성 검사 (Validation)
- [ ] 고객명 비어 있을 때 저장 시 alert 및 저장 차단
- [ ] 고객 종류별 필수 식별번호(개인→주민, 외국인→외국인등록번호 등) 누락 시 메시지 및 차단
- [ ] 형식 오류(주민 6+7자리, 외국인등록번호 13자리 등) 시 메시지 및 차단

## 3. 상세 조회 (Read / Detail)
- [ ] 리스트에서 행 클릭 시 상세 모달 오픈 (관리 컬럼·삭제 버튼 클릭 시에는 미오픈)
- [ ] 고객·납부·사은품·본사 사은품·상품·번호이동 등 **저장된 모든 정보가 필드에 누락 없이** 표시되는지 확인
- [ ] 만기일(Read-only)이 개통일+약정으로 계산되어 표시되는지 확인

## 4. 정보 수정 (Update)
- [ ] 상세 모달에서 값 변경 후 저장 시 "수정되었습니다." 표시
- [ ] **기존 값이 잘못 덮어씌워지지 않고** 변경한 값만 정확히 반영되는지 확인
- [ ] 저장 후 리스트 및 재조회 시 변경 내용 반영 확인
- [ ] **DB 검증**: 동일 CUST_ID로 `customer-db-integrity-check.sql` 1·2번 블록 실행  
  → JOIN_DATE, RECEIPT_DT, 상품 OPEN_DATE/CANCEL_DATE 등 수정값 반영 여부 확인

## 5. 데이터 삭제 (Delete)
- [ ] 리스트에서 "삭제" 버튼 클릭 시 컨펌 창 노출
- [ ] 확인 시 삭제 처리 후 리스트에서 해당 행 제거
- [ ] **DB 검증**: `customer-db-integrity-check.sql` 3번 블록  
  → 삭제한 CUST_ID가 TB_CUSTOMER에 없고, TB_CUST_PAYMENT/GIFT/PRODUCT/MNP에 해당 CUST_ID 행이 **0건**인지 확인 (고아 데이터 없음)
- [ ] **전체 고아 검사**: 동일 문서 3-2 쿼리 실행 → 결과 0건이어야 함

## 6. 엑셀 및 필터
- [ ] 엑셀 다운로드 시 현재 리스트와 동일한 데이터(포맷 포함)로 파일 생성되는지 확인
- [ ] 상단 필터(고객명, 통신사, 개통상태, 기간 등) 적용 시 리스트가 AND 조건으로 필터링되는지 확인
- [ ] 필터 결과가 DB 실제 데이터와 일치하는지 필요 시 SQL(4·5번 블록)로 대조

## 연관 테이블 요약
| 테이블 | 설명 | FK |
|--------|------|-----|
| TB_CUSTOMER | 고객 마스터 (CUST_KIND, JOIN_DATE, RECEIPT_DT, INSTALL_SCHED_DT 등) | - |
| TB_CUST_PRODUCT | 상품 (개통일/해지일, 가입번호 등) | CUST_ID |
| TB_CUST_PAYMENT | 납부 정보 | CUST_ID |
| TB_CUST_GIFT | 사은품(GENERAL/HEAD) | CUST_ID |
| TB_CUST_MNP | 번호이동 | CUST_ID |

삭제 시 애플리케이션에서 **자식 테이블을 먼저 삭제한 뒤** TB_CUSTOMER를 삭제하므로, DB에 CASCADE가 없어도 고아 데이터가 발생하지 않습니다.
