# 현재 작업 맥락 (Active Context)

## 지금 초점
- 고객관리(개인 고객) 및 **고객 상세**는 `IndividualCustomerManagement.vue` 한 컴포넌트에서 처리. 상세는 별도 페이지가 아니라 모달로 제공.

## 최근 결정
- 고객 상세 = 그리드 클릭/수정 → 상세 모달. 상품권 반환여부(voucherReturnYn) 체크박스는 상세 모드일 때만 표시. (memory-bank에 고객 상세 구조 기록해 두었음.)

## 사용자 요청 정리 (기억해 둔 것)
- 사이드바: 공지사항, 고객관리, 인사관리만. 고객관리는 **토글** (펼치면 개인고객·협력점 고객), **디폴트 개인고객** 선택(/customers → /customers/individual).
- 개인고객 화면: **AG Grid**로 리스트. **헤더(컬럼)는 다 나오게** (sizeColumnsToFit 미사용, 가로 스크롤). 위 **필터 줄이고**, **가운데 리스트만 스크롤**.
- 고객 상세: 모달, 상품권 반환여부 체크박스. (상세는 systemPatterns.md·progress.md에 기록)
- **고객 등록/상세 폼**: **개통일자**(날짜 입력, `form.customer.openDate` → DB `TB_CUSTOMER.INSTALL_SCHED_DT`), **개통상태**(셀렉트박스, `form.customer.status`, CUST_STATUS 공통코드) 반드시 포함.

## 다음 할 일
- (다음에 진행할 작업 목록.)

## 가입번호(SUBSCRIPTION_NO) 정리
- **DB 컬럼**: TB_CUST_PRODUCT.SUBSCRIPTION_NO (동일 컬럼)
- **리스트**: selectCustomerList 서브쿼리 P에서 P1.SUBSCRIPTION_NO → AS subscription_no (대표 상품 1건만)
- **상세**: selectProductsByCustId에서 SUBSCRIPTION_NO AS subscription_no → CustProductDto.subscriptionNo
- **등록/수정**: insertProduct/updateProduct에서 SUBSCRIPTION_NO 매핑
- **프론트**: 그리드 컬럼 field `subscriptionNo`, 상품 행 입력란 `row.subscriptionNo`

---
*이 파일은 현재 작업 포커스와 단기 결정 사항을 유지합니다. 작업이 바뀔 때마다 업데이트하면 AI가 맥락을 유지하는 데 도움이 됩니다.*
