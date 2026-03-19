# 시스템 패턴 (System Patterns)

## 아키텍처
- **프론트엔드**: SPA (Vue 3), `front-end/` 루트
  - `src/views/`, `src/components/`, `src/router/`, `src/store/`
- **백엔드**: REST API (Spring Boot), `back-end/` 루트
  - `com.sys.managesys`: 공통 config, auth, dto, mapper

## 인증
- JWT 발급/검증: `JwtProvider`, `JwtAuthenticationFilter`
- 로그인: `AuthController`, `AuthService`, `LoginRequest`/`LoginResponse`
- 사용자 상세: `CustomUserDetailsService`, `CustomUserDetails`
- **로그인/로그아웃 히스토리**: **TB_USER_LOGIN_HISTORY** 테이블 사용 (HISTORY_ID, USER_ID, EVENT_TYPE, IP_ADDRESS, USER_AGENT, CREATED_AT). EVENT_TYPE = 'LOGIN' | 'LOGOUT'. 로그인 성공 시 `AuthService.login()`에서 INSERT, 로그아웃 시 `POST /api/auth/logout` → `AuthService.recordLogout()`. 매퍼: `LoginHistoryMapper` (INSERT INTO TB_USER_LOGIN_HISTORY)

## 사이드바 (사용자 요청 구조)
- **메뉴는 3개만**: 공지사항, 고객관리, 인사관리 (가망고객·고객센터·정산관리·부가서비스 없음)
- **고객관리**는 **토글 형식**: 제목 클릭 시 서브메뉴 접기/펼치기. 펼치면 **디폴트로 개인고객이 선택**되도록 `/customers`는 `/customers/individual`로 리다이렉트(router). 서브메뉴: 개인고객·협력점 고객. 고객관리 하위 경로일 때 토글 자동 펼침.
- 고객관리 서브메뉴 2개:
  1. **개인고객** → `/customers/individual` (IndividualCustomerManagement.vue)
  2. **협력점 고객** → `/customers/partner` (PlaceholderPage.vue)
- 컴포넌트: `SideBar.vue` — 공지사항(단일 링크), 고객관리(토글 버튼 + ChevronDown, v-show로 서브 표시), 인사관리(단일 링크)
- **개인고객 화면(IndividualCustomerManagement.vue)**: 리스트는 **AG Grid**로 표시. **AG Grid 리스트에는 헤더(컬럼)를 다 나오게** — `sizeColumnsToFit()` 사용하지 않음, 컬럼마다 width/minWidth 유지 → 가로 스크롤로 모든 헤더 표시. **위 필터는 줄인 형태**, **가운데 리스트(그리드) 영역만 스크롤**되도록 레이아웃. (`.customer-page` flex column, `.grid-section` flex 1 + min-height 0, `.grid-fill`, App.vue `.contents` min-height 0 + overflow hidden)

## 고객관리 / 고객 상세
- **경로**: `/customers` → 리다이렉트 `/customers/individual`, 컴포넌트 `IndividualCustomerManagement.vue`
- **고객 상세**: 별도 URL이 아니라 **같은 페이지 내 모달**로 제공
  - 그리드 행 클릭 또는 "수정" 버튼 → `openDetailModal(custId)` → `GET /api/customers/detail?custId=` 호출 후 모달 오픈
  - 모달 제목: `modalMode === 'detail'` 일 때 "고객 상세", 등록 시 "고객 등록"
- **상세 모달 전용 UI**
  - **상품권 반환여부**: `v-if="modalMode === 'detail'"` 인 행에 체크박스 `form.customer.voucherReturnYn` (Y/N), 라벨 "상품권 반환함"
- **고객 정보 폼 공통**: **개통일자** — `<input type="date">` `form.customer.openDate` (API/DB: `openDate` → `TB_CUSTOMER.INSTALL_SCHED_DT`). **개통상태** — `<select>` `form.customer.status`, 옵션은 CUST_STATUS 공통코드(`statusCodes`).
- **저장 시**: 상세 모드면 `PUT /api/customers/update`, 등록 모드면 `POST /api/customers/register`
- **주민번호**: 폼은 앞6자리/뒤7자리 분리 입력(ssn1, ssn2, holderSsn1/2, ownerSsn1/2). API 전송 시 `buildRegisterPayload`에서 `joinSsn`으로 합쳐 `ssnEnc`, `holderSsnEnc`, `ownerSsnEnc` 로 보냄

## 공통코드 (TB_GROUP_CODE / TB_COMMON_CODE)
- **테이블**: `TB_GROUP_CODE`(그룹 마스터), `TB_COMMON_CODE`(GROUP_CODE, CODE_VALUE, CODE_NAME, SORT_ORDER, USE_YN)
- **API**: `POST /api/codes/list` — body: `{ "groupCode": "CUST_STATUS" }`, 응답: `[{ "codeValue", "codeName", "sortOrder" }, ...]`
- **구현**: `CodeController`, `CommonCodeMapper`, `CommonCodeMapper.xml`, `CommonCodeDto`
- **프론트 연동**: 개인고객관리(`IndividualCustomerManagement.vue`)의 `loadCodeList(groupCode)` → 위 API 호출.
- **그룹코드 용도 (화면 ↔ TB_COMMON_CODE)**  
  | 화면/용도 | 그룹코드 | 비고 |
  |-----------|----------|------|
  | 지급은행 | BANK_CODE | 사은품계좌 등 |
  | 청구서 종류 | BILL_TYPE | |
  | 요금 감면 | DISCOUNT_GB | |
  | 지급처 | PAY_SOURCE | |
  | 본사 사은품 | HEAD_GIFT | |
  | 지역 선택 | REGION_CODE | |
  | 진행 상태 (개통상태) | CUST_STATUS | 접수, 개통완료 등 |
  | 고객 종류 | CUST_TYPE | 개인, 개인사업자, 법인 등 |
  | 고객 인증 | CUST_AUTH | 주민발급일자, 면허번호 등 |
  | 통신사/회사 | COMPANY_CODE | 필터·폼 통신사/회사선택 |
  | 사용자 권한 | USER_ROLE | |
- **표시 규칙**: DB에는 CODE_VALUE 저장, **리스트·엑셀에는 CODE_NAME으로 표시**. 고객 목록 API는 공통코드 JOIN으로 statusName, hpCarrierName, custTypeName, paySource 등 CODE_NAME 반환. 그리드/엑셀은 이 *Name 필드 사용(동일 데이터 기준).
- **필터·모달**: 필터(개통상태, 통신사)와 등록/상세 모달 셀렉트는 모두 `POST /api/codes/list`로 공통코드 조회. option value=codeValue, text=codeName. 검색/저장 시 CODE_VALUE 사용 (CUST_STATUS: RECEIPT/COMPLETED 등, 통신사: COMPANY_CODE의 SKB/KT/LGU 등).
- **상품 유형**: 단독·DPS·TPS만 허용. PROD_COMB_GB 기준 S0=단독, D0=DPS, T0=TPS. 프론트 `productTypeOptions` 고정, 저장/상세 시 prodGb는 S0/D0/T0.

## API·데이터
- MyBatis 매퍼: `resources/mapper/*.xml`, `*Mapper.java`
- DTO: `common.dto` 패키지 (UserDto, UserResponse, LoginRequest 등)
- 고객: `CustomerController` (`/api/customers`), `CustomerMapper.xml`, `CustomerDto`, `CustomerDetailResponse`, `CustomerRegisterRequest`, `CustPaymentDto`, `CustGiftDto`, `CustMnpDto`, `CustProductDto`

## 기술 결정
- DB: MariaDB + log4jdbc (SQL 로깅)
- 프론트 상태: Pinia
- 그리드/테이블: AG Grid (ag-grid-vue3)
