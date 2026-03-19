# 진행 상황 (Progress)

## 완료된 작업
- 2026-03-02: **비밀번호 초기화 계정 로그인 시 재설정 모달** — (1) TB_USER에 PASSWORD_RESET_YN 컬럼 추가(docs/add-password-reset-yn.sql). (2) 관리자 비밀번호 초기화 시 PASSWORD_RESET_YN='Y' 설정. (3) 로그인 응답에 mustChangePassword 포함, POST /api/auth/change-password(인증 필수)로 재설정 시 비밀번호 갱신 및 플래그 해제. (4) 프론트: 로그인 성공 후 mustChangePassword이면 비밀번호 재설정 모달(ChangePasswordModal) 표시, 재설정 완료 후 메인 이동. 새로고침 등으로 인증만 된 상태에서도 mustChangePassword이면 App.vue에서 전역 모달 표시.
- 2026-03-02: **개통일자/개통상태 상품 단위로 이전** — (1) 고객 정보 섹션에서 개통일자·개통상태 행 제거(개통일자는 상품 정보에만 존재). (2) 개통상태를 상품 정보(1:M) 쪽으로 이동: 상품 테이블에 개통상태(OPEN_STATUS) 컬럼 추가, 각 상품 행별 개통상태 선택. (3) 백엔드: TB_CUST_PRODUCT에 OPEN_STATUS 컬럼 추가 스크립트(docs/add-product-status-column.sql), 리스트/필터는 첫 번째 상품의 P.OPEN_STATUS·P.OPEN_DATE 사용, insertProduct에 openStatus 반영. (4) 프론트: buildRegisterPayload에서 고객 openDate는 null 전송, 상품 payload에 openStatus 포함.
- 2026-03-02: **고객 등록 전체 항목 테스트** — (1) 본사 사은품 상세 매핑: `mapDetailToForm`에서 headGift **payDoneDate, paySchedDate** 누락 수정. (2) 납부/사은품/번호이동 **remark** 필드: getEmptyPayment, getEmptyGift, getEmptyMnp에 `remark: ''` 추가해 payload로 전송. (3) 고객 등록 모달에 **테스트 데이터 채우기** 버튼 추가(등록 모드에서만 노출). (4) 전체 항목 입력 후 등록 → 상세 조회로 DB 저장 검증 완료.
- 2026-03-02: 언도로 인한 복구 — App.vue에 `isPublicLayout` 계산 속성 추가(공개 레이아웃 처리), IndividualCustomerManagement 상세 모달 MNP 주민번호(ownerSsn1/2) 매핑 수정.
- 고객 상세: 개인 고객관리 그리드에서 행 클릭/수정 시 **고객 상세 모달** 오픈 (같은 컴포넌트, `modalMode='detail'`). 상품권 반환여부 체크박스는 상세 모달에서만 노출. 저장 시 payload에 주민번호·은행·사은품·번호이동 쪽 `holderSsnEnc`/`ownerSsnEnc` 결합 전송.
- 사이드바: 사용자 요청대로 **공지사항, 고객관리(개인고객 + 협력점 고객), 인사관리** 3개만 표시. 고객관리는 그룹 제목 + 서브메뉴 2개 구조. (systemPatterns.md에 상세 기록)
- 공통코드 연동: `TB_COMMON_CODE` 조회용 **POST /api/codes/list** 구현. 개인고객관리 화면에서 해당 그룹 코드로 select 옵션 로드.
- 개인고객 AG Grid: **헤더(컬럼)는 다 나오게** — sizeColumnsToFit() 미사용, 컬럼 width/minWidth 유지, 가로 스크롤로 전부 표시. (systemPatterns.md에 기록)
- 로그인/로그아웃 히스토리: **TB_USER_LOGIN_HISTORY** 테이블 사용으로 원복. 매퍼 XML에서 INSERT 대상 테이블·컬럼을 TB_USER_LOGIN_HISTORY, EVENT_TYPE 기준으로 수정. 리스트 미표시 대비 AG Grid 영역에 min-height: 400px 적용.
- 가입번호(SUBSCRIPTION_NO): 리스트·상세·등록 모두 **TB_CUST_PRODUCT.SUBSCRIPTION_NO** 동일 컬럼 사용. 매퍼에서 MyBatis mapUnderscoreToCamelCase 적용을 위해 리스트/상품 조회 시 `AS subscription_no` 별칭 사용. 리스트 서브쿼리 P에 USE_YN 조건 추가.

## 진행 중
- (현재 진행 중인 작업.)

## 알려진 이슈
- (버그, 기술 부채, 제약 사항.)

## 다음 마일스톤
- (다음 릴리스나 목표.)

---
*이 파일은 프로젝트 상태와 이슈를 추적합니다. 주기적으로 업데이트하면 일관된 맥락을 유지할 수 있습니다.*
