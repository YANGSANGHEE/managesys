<template>
  <div class="customer-page">
    <div class="content-header">
      <h2>협력점 고객관리</h2>
    </div>

    <section class="search-section card">
      <div class="search-grid">
        <div class="input-box">
          <label>고객명</label>
          <input v-model="searchQuery.searchCustName" @keyup.enter="onSearch" placeholder="고객명 입력" />
        </div>
        <div class="input-box">
          <label>통신사</label>
          <select v-model="searchQuery.searchHpCarrier">
            <option value="">전체</option>
            <option v-for="c in companyCodes" :key="c.codeValue" :value="c.codeValue">{{ c.codeName }}</option>
          </select>
        </div>
        <div class="status-period-wrap">
          <div class="input-box narrow">
            <label>개통상태</label>
            <select v-model="searchQuery.searchStatus">
              <option value="">전체</option>
              <option v-for="c in statusCodes" :key="c.codeValue" :value="c.codeValue">{{ c.codeName }}</option>
            </select>
          </div>
          <div class="input-box period-group">
            <label>기간</label>
            <div class="period-inline">
              <select v-model="searchQuery.searchPeriodType" class="period-type-select">
                <option value="">등록일</option>
                <option value="RECEIPT">접수일</option>
                <option value="OPEN">개통일</option>
                <option value="JOIN">가입일</option>
                <option value="CANCEL">해지일</option>
              </select>
              <span class="period-sep">~</span>
              <input type="date" v-model="searchQuery.searchCreatedAtFrom" />
              <input type="date" v-model="searchQuery.searchCreatedAtTo" />
            </div>
          </div>
        </div>
      </div>
      <div class="search-footer">
        <div class="search-buttons">
          <button class="btn-reset" @click="resetSearch">초기화</button>
          <button class="btn-search" @click="onSearch">Q 검색</button>
        </div>
        <div class="right-actions">
          <select v-model="pageSize" class="page-size-select" @change="onPageSizeChange">
            <option :value="10">10개 출력</option>
            <option :value="20">20개 출력</option>
            <option :value="30">30개 출력</option>
            <option :value="50">50개 출력</option>
          </select>
          <button class="btn-excel" @click="onExcelSave">엑셀저장</button>
          <button class="btn-add" @click="onCustomerRegister">고객등록</button>
        </div>
      </div>
    </section>

    <section class="grid-section card">
      <ag-grid-vue
        class="ag-theme-alpine grid-fill"
        :columnDefs="columnDefs"
        :rowData="rowData"
        :defaultColDef="defaultColDef"
        :pagination="true"
        :paginationPageSize="pageSize"
        :paginationPageSizeSelector="[10, 20, 30, 50]"
        :rowSelection="'multiple'"
        @grid-ready="onGridReady"
        @row-clicked="onRowClicked"
      />
    </section>

    <div v-if="totalCount >= 0" class="pagination-info">
      총 <strong>{{ totalCount }}</strong> 건
    </div>

    <!-- 엑셀 저장 모달 -->
    <div v-if="showExcelModal" class="modal-overlay" >
      <div class="modal-excel">
        <div class="modal-excel-header">
          <h3>엑셀 저장</h3>
          <button type="button" class="modal-close" @click="closeExcelModal">&times;</button>
        </div>
        <div class="modal-excel-body">
          <div class="excel-filter-item">
            <label>기간</label>
            <div class="period-inline">
              <select v-model="excelSearchPeriodType" class="excel-period-type-select">
                <option value="">등록일</option>
                <option value="RECEIPT">접수일</option>
                <option value="OPEN">개통일</option>
                <option value="JOIN">가입일</option>
                <option value="CANCEL">해지일</option>
              </select>
              <span class="period-sep">~</span>
              <input type="date" v-model="excelSearchDateFrom" />
              <input type="date" v-model="excelSearchDateTo" />
            </div>
          </div>
          <div class="excel-filter-item">
            <label>다운로드할 건수</label>
            <select v-model.number="excelRowCount" class="excel-row-select">
              <option v-for="n in EXCEL_ROW_OPTIONS" :key="n" :value="n">{{ n }}개</option>
            </select>
          </div>
        </div>
        <div class="modal-excel-footer">
          <button type="button" class="btn-cancel" @click="closeExcelModal">취소</button>
          <button type="button" class="btn-download" @click="doExcelDownload" :disabled="excelDownloading">다운로드</button>
        </div>
      </div>
    </div>

    <!-- 고객 등록 모달 (전체 섹션) -->
    <div v-if="showCustomerModal" class="modal-overlay" >
      <div class="modal-customer-reg">
        <div class="modal-reg-header">
          <h3>{{ modalMode === 'detail' ? '고객 상세' : '고객 등록' }}</h3>
          <button type="button" class="modal-close" @click="showCustomerModal = false">&times;</button>
        </div>
        <div class="modal-reg-body">
          <!-- 1. 고객 정보 -->
          <section class="reg-section">
            <h4 class="reg-section-title">고객 정보</h4>
            <table class="reg-table">
              <tbody>
              <tr>
                <th>고객명 <span class="required">*</span></th>
                <td><input v-model="form.customer.custName" placeholder="고객명" /></td>
                <th>대표자</th>
                <td><input v-model="form.customer.repName" placeholder="대표자 (사업자/법인)" /></td>
              </tr>
              <tr>
                <th>고객종류</th>
                <td colspan="3">
                  <select v-model="form.customer.custType" class="cust-type-select">
                    <option value="">선택</option>
                    <option v-for="o in custTypeCodes" :key="o.codeValue" :value="o.codeValue">{{ o.codeName }}</option>
                  </select>
                </td>
              </tr>
              <tr v-if="showSsn || showBizNo">
                <template v-if="showSsn">
                  <th>주민번호</th>
                  <td :colspan="showBizNo ? 1 : 3">
                    <div class="input-split-with-btn">
                      <div class="input-split">
                        <input v-model="form.customer.ssn1" type="text" placeholder="앞 6자리" maxlength="6" />
                        <span>-</span>
                        <input v-model="form.customer.ssn2" type="text" placeholder="뒤 7자리" maxlength="7" />
                      </div>
                      <button type="button" class="btn-inline" @click="copySsn">복사</button>
                    </div>
                  </td>
                </template>
                <template v-if="showBizNo">
                  <th>사업자번호</th>
                  <td :colspan="showSsn ? 1 : 3">
                    <input
                      :value="form.customer.bizNo"
                      @input="form.customer.bizNo = formatBizNo($event.target.value)"
                      type="text"
                      placeholder="000-00-00000"
                      maxlength="12"
                      inputmode="numeric"
                    />
                  </td>
                </template>
              </tr>
              <tr v-if="showCorpNo || showForeignerNo">
                <template v-if="showCorpNo">
                  <th>법인번호</th>
                  <td :colspan="showForeignerNo ? 1 : 3">
                    <input
                      :value="form.customer.corpNo"
                      @input="form.customer.corpNo = formatCorpNo($event.target.value)"
                      type="text"
                      placeholder="123456-1234567"
                      maxlength="14"
                      inputmode="numeric"
                    />
                  </td>
                </template>
                <template v-if="showForeignerNo">
                  <th>외국인 등록번호</th>
                  <td :colspan="showCorpNo ? 1 : 3">
                    <input
                      :value="form.customer.foreignerRegNo"
                      @input="form.customer.foreignerRegNo = formatForeignerRegNo($event.target.value)"
                      type="text"
                      placeholder="123456-7890123"
                      maxlength="14"
                      inputmode="numeric"
                    />
                  </td>
                </template>
              </tr>
              <tr>
                <th>우편번호</th>
                <td><input v-model="form.customer.zipCode" placeholder="우편번호" /></td>
                <th>주소</th>
                <td><input v-model="form.customer.addr" placeholder="주소" /></td>
              </tr>
              <tr>
                <th>상세주소</th>
                <td colspan="3">
                  <div class="input-with-btn">
                    <input v-model="form.customer.addrDetail" placeholder="상세주소" />
                    <button type="button" class="btn-inline btn-search-inline" @click="onSearchAddress">Q 검색</button>
                  </div>
                </td>
              </tr>
              <tr>
                <th>전화</th>
                <td>
                  <input
                    :value="form.customer.telNo"
                    @input="form.customer.telNo = formatPhone($event.target.value)"
                    type="text"
                    placeholder="010-0000-0000"
                    maxlength="13"
                    inputmode="numeric"
                  />
                </td>
                <th>핸드폰</th>
                <td>
                  <div class="input-with-select">
                    <select v-model="form.customer.hpCarrier" class="carrier-select">
                      <option value="">통신사</option>
                      <option v-for="c in companyCodes" :key="c.codeValue" :value="c.codeValue">{{ c.codeName }}</option>
                    </select>
                    <input
                      :value="form.customer.hpNo"
                      @input="form.customer.hpNo = formatPhone($event.target.value)"
                      type="text"
                      placeholder="010-0000-0000"
                      maxlength="13"
                      inputmode="numeric"
                    />
                  </div>
                </td>
              </tr>
              <tr>
                <th>이메일</th>
                <td><input v-model="form.customer.email" type="email" placeholder="이메일" /></td>
                <th>고객 인증</th>
                <td>
                  <div class="input-with-select auth-fields">
                    <select v-model="form.customer.custAuthType" class="auth-type-select">
                      <option value="">고객 인증</option>
                      <option v-for="o in custAuthCodes" :key="o.codeValue" :value="o.codeValue">{{ o.codeName }}</option>
                    </select>
                    <input v-model="form.customer.custAuthVal" placeholder="인증값" />
                  </div>
                </td>
              </tr>
              <tr>
                <th>상담원</th>
                <td colspan="3">
                  <select v-model="selectedCounselorUserId" @change="onCounselorChange" class="counselor-select">
                    <option :value="null">선택</option>
                    <option v-for="u in counselorOptions" :key="u.userId" :value="u.userId">{{ u.userName || u.userId }}</option>
                  </select>
                  <input v-model="form.customer.deptId" type="hidden" />
                  <input v-model="form.customer.assignedUserId" type="hidden" />
                </td>
              </tr>
              <tr>
                <th>가입일자</th>
                <td><input v-model="form.customer.joinDate" type="date" placeholder="가입일자" /></td>
                <th>접수일</th>
                <td><input v-model="form.customer.receiptDate" type="date" placeholder="접수일" /></td>
              </tr>
              <tr v-if="modalMode === 'detail'">
                <th>상품권 반환여부</th>
                <td colspan="3">
                  <label class="check-label">
                    <input v-model="form.customer.voucherReturnYn" type="checkbox" true-value="Y" false-value="N" />
                    상품권 반환함
                  </label>
                </td>
              </tr>
              </tbody>
            </table>
          </section>

          <!-- 2. 납부 정보 -->
          <section class="reg-section">
            <h4 class="reg-section-title">납부 정보</h4>
            <table class="reg-table">
              <tbody>
              <tr>
                <th>납부방법</th>
                <td colspan="3">
                  <div class="radio-group">
                    <label><input v-model="form.payment.payMethod" type="radio" value="BANK" /> 계좌이체</label>
                    <label><input v-model="form.payment.payMethod" type="radio" value="CARD" /> 카드납부</label>
                    <label><input v-model="form.payment.payMethod" type="radio" value="GIRO" /> 지로납부</label>
                    <label><input v-model="form.payment.payMethod" type="radio" value="COMB" /> 합산청구</label>
                  </div>
                </td>
              </tr>
              <tr>
                <th>청구서 종류</th>
                <td>
                  <select v-model="form.payment.billType">
                    <option value="">::선택::</option>
                    <option v-for="o in billTypeCodes" :key="o.codeValue" :value="o.codeValue">{{ o.codeName }}</option>
                  </select>
                </td>
                <th>요금 감면</th>
                <td>
                  <select v-model="form.payment.discountType">
                    <option value="">해당없음</option>
                    <option v-for="o in discountTypeCodes" :key="o.codeValue" :value="o.codeValue">{{ o.codeName }}</option>
                  </select>
                </td>
              </tr>
              <tr>
                <th>예금주</th>
                <td><input v-model="form.payment.accountHolder" placeholder="예금주" :readonly="sameAsCustomerForPayment" /></td>
                <th>은행 정보</th>
                <td>
                  <select v-model="form.payment.bankCardName">
                    <option value="">지급은행</option>
                    <option v-for="o in bankCodes" :key="o.codeValue" :value="o.codeValue">{{ o.codeName }}</option>
                  </select>
                </td>
              </tr>
              <tr>
                <th>계좌번호</th>
                <td>
                  <input
                    :value="form.payment.accountCardNo"
                    @input="form.payment.accountCardNo = formatAccountNo($event.target.value)"
                    type="text"
                    placeholder="숫자만 입력"
                    maxlength="20"
                    inputmode="numeric"
                  />
                </td>
                <th>{{ isForeignerCustomer ? '외국인 등록번호' : '은행 주민번호' }}</th>
                <td>
                  <template v-if="isForeignerCustomer">
                    <input
                      :value="form.payment.holderSsn1 + (form.payment.holderSsn2 ? '-' + form.payment.holderSsn2 : '')"
                      @input="setPaymentHolderIdNo($event.target.value)"
                      type="text"
                      placeholder="123456-7890123"
                      maxlength="14"
                      inputmode="numeric"
                      :readonly="sameAsCustomerForPayment"
                    />
                  </template>
                  <template v-else>
                    <div class="input-split input-ssn">
                      <input v-model="form.payment.holderSsn1" type="text" placeholder="앞 6자리" maxlength="6" :readonly="sameAsCustomerForPayment" />
                      <span>-</span>
                      <input v-model="form.payment.holderSsn2" type="text" placeholder="뒤 7자리" maxlength="7" :readonly="sameAsCustomerForPayment" />
                    </div>
                  </template>
                  <label class="check-label same-as-check"><input v-model="sameAsCustomerForPayment" type="checkbox" /> 위 정보와 동일</label>
                </td>
              </tr>
              <tr>
                <th>고객과 관계</th>
                <td colspan="3"><input v-model="form.payment.relationWithCust" placeholder="본인, 가족 등" /></td>
              </tr>
              </tbody>
            </table>
          </section>

          <!-- 3. 사은품 정보 -->
          <section class="reg-section">
            <h4 class="reg-section-title">사은품 정보</h4>
            <table class="reg-table">
              <tbody>
              <tr>
                <th>사은품 명</th>
                <td><input v-model="form.gift.giftName" placeholder="사은품 명" /></td>
                <th>사은품 금액</th>
                <td><span class="amount-with-unit"><input :value="formatAmount(form.gift.giftAmount)" @input="form.gift.giftAmount = parseAmount($event.target.value)" type="text" placeholder="0" inputmode="numeric" /><span class="unit">원</span></span></td>
              </tr>
              <tr>
                <th>추가사은품 금액</th>
                <td><span class="amount-with-unit"><input :value="formatAmount(form.gift.addGiftAmount)" @input="form.gift.addGiftAmount = parseAmount($event.target.value)" type="text" placeholder="0" inputmode="numeric" /><span class="unit">원</span></span></td>
                <th>지급예정일</th>
                <td><input v-model="form.gift.paySchedDate" type="date" /></td>
              </tr>
              <tr>
                <th>지급일</th>
                <td><input v-model="form.gift.payDoneDate" type="date" placeholder="지급일" /></td>
                <th>지급처</th>
                <td>
                  <select v-model="form.gift.paySource">
                    <option value="">지급처 선택</option>
                    <option v-for="o in paySourceCodes" :key="o.codeValue" :value="o.codeValue">{{ o.codeName }}</option>
                  </select>
                </td>
              </tr>
              <tr>
                <th>사은품계좌</th>
                <td colspan="3">
                  <select v-model="form.gift.bankName" :disabled="sameAsPaymentForGift">
                    <option value="">지급은행</option>
                    <option v-for="o in bankCodes" :key="o.codeValue" :value="o.codeValue">{{ o.codeName }}</option>
                  </select>
                </td>
              </tr>
              <tr>
                <th>예금주</th>
                <td>
                  <input v-model="form.gift.accountHolder" placeholder="예금주" :readonly="sameAsPaymentForGift" />
                  <label class="check-label same-as-check"><input v-model="sameAsPaymentForGift" type="checkbox" /> 위 은행과 동일</label>
                </td>
                <th>계좌번호</th>
                <td>
                  <input
                    :value="form.gift.accountNo"
                    @input="form.gift.accountNo = formatAccountNo($event.target.value)"
                    type="text"
                    placeholder="숫자만 입력"
                    maxlength="20"
                    inputmode="numeric"
                    :readonly="sameAsPaymentForGift"
                  />
                </td>
              </tr>
              <tr>
                <th>{{ isForeignerCustomer ? '외국인 등록번호' : '주민번호' }}</th>
                <td colspan="3">
                  <template v-if="isForeignerCustomer">
                    <input
                      :value="form.gift.holderSsn1 + (form.gift.holderSsn2 ? '-' + form.gift.holderSsn2 : '')"
                      @input="setGiftHolderIdNo($event.target.value)"
                      type="text"
                      placeholder="123456-7890123"
                      maxlength="14"
                      inputmode="numeric"
                      :readonly="sameAsPaymentForGift"
                    />
                  </template>
                  <template v-else>
                    <div class="input-split input-ssn">
                      <input v-model="form.gift.holderSsn1" type="text" placeholder="앞 6자리" maxlength="6" :readonly="sameAsPaymentForGift" />
                      <span>-</span>
                      <input v-model="form.gift.holderSsn2" type="text" placeholder="뒤 7자리" maxlength="7" :readonly="sameAsPaymentForGift" />
                    </div>
                  </template>
                </td>
              </tr>
              </tbody>
            </table>
          </section>

          <!-- 4. 본사 사은품 -->
          <section class="reg-section">
            <h4 class="reg-section-title">본사 사은품</h4>
            <table class="reg-table">
              <tbody>
              <tr>
                <th>본사사은품 명</th>
                <td>
                  <select v-model="form.headGift.giftName">
                    <option value="">본사사은품</option>
                    <option v-for="o in headGiftCodes" :key="o.codeValue" :value="o.codeValue">{{ o.codeName }}</option>
                  </select>
                </td>
                <th>본사사은품 금액</th>
                <td>
                  <span class="amount-with-unit">
                    <input :value="formatAmount(form.headGift.giftAmount)" @input="form.headGift.giftAmount = parseAmount($event.target.value)" type="text" placeholder="0" inputmode="numeric" />
                    <span class="unit">원</span>
                  </span>
                </td>
              </tr>
              </tbody>
            </table>
          </section>

          <!-- 5. 상품 정보 (구분: 단독 1줄 / DPS 2줄 / TPS 3줄 고정) -->
          <section class="reg-section">
            <h4 class="reg-section-title">상품 정보</h4>
            <table class="reg-table product-table">
              <thead>
                <tr>
                  <th>구분</th>
                  <td colspan="12">
                    <select v-model="productType" @change="onProductTypeChange">
                      <option v-for="opt in productTypeOptions" :key="opt.codeValue" :value="opt.codeValue">{{ opt.codeName }}</option>
                    </select>
                  </td>
                </tr>
                <tr>
                  <th>상품</th>
                  <th>회사</th>
                  <th>지역</th>
                  <th>가입번호</th>
                  <th>상품명</th>
                  <th>상품옵션</th>
                  <th>약정</th>
                  <th>개통상태</th>
                  <th>개통일자</th>
                  <th>해지일자</th>
                  <th>만기일</th>
                  <th>셋탑</th>
                  <th>부가서비스</th>
                  <th>회선</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(row, idx) in productRows" :key="idx">
                  <th>상품 {{ idx + 1 }}</th>
                  <td>
                    <select v-model="row.company" class="product-company-select">
                      <option value="">회사 선택</option>
                      <option v-for="o in companyCodes" :key="o.codeValue" :value="o.codeValue">{{ o.codeName }}</option>
                    </select>
                  </td>
                  <td>
                    <select v-model="row.region" class="product-region-select">
                      <option value="">지역선택</option>
                      <option v-for="o in regionCodes" :key="o.codeValue" :value="o.codeValue">{{ o.codeName }}</option>
                    </select>
                  </td>
                  <td><input v-model="row.subscriptionNo" type="text" placeholder="가입번호" /></td>
                  <td><input v-model="row.product" type="text" placeholder="상품" /></td>
                  <td><input v-model="row.productOption" type="text" placeholder="상품옵션" /></td>
                  <td><input v-model="row.contract" type="text" placeholder="약정(예: 24개월)" /></td>
                  <td>
                    <select v-model="row.openStatus" class="status-select">
                      <option value="">선택</option>
                      <option v-for="c in statusCodes" :key="c.codeValue" :value="c.codeValue">{{ c.codeName }}</option>
                    </select>
                  </td>
                  <td><input v-model="row.openDate" type="date" placeholder="개통일자" /></td>
                  <td><input v-model="row.cancelDate" type="date" placeholder="해지일자" /></td>
                  <td class="readonly-cell">{{ computedMaturityDate(row.openDate, row.contract) || '-' }}</td>
                  <td><input v-model="row.setTop" type="text" placeholder="셋탑박스" /></td>
                  <td><input v-model="row.vas" type="text" placeholder="부가서비스" /></td>
                  <td><input v-model.number="row.lineCount" type="number" min="1" class="input-line" /> 회선</td>
                </tr>
              </tbody>
            </table>
          </section>

          <!-- 6. 번호이동 -->
          <section class="reg-section">
            <h4 class="reg-section-title">번호이동</h4>
            <table class="reg-table">
              <tbody>
              <tr>
                <th>위 고객정보와 동일</th>
                <td>
                  <label class="check-label">
                    <input v-model="form.mnp.isSameAsCust" type="checkbox" true-value="Y" false-value="N" />
                    동일
                  </label>
                </td>
                <th>전화 번호</th>
                <td><input v-model="form.mnp.mnpTelNo" placeholder="번호이동 전화번호" :readonly="form.mnp.isSameAsCust === 'Y'" /></td>
              </tr>
              <tr>
                <th>명의자</th>
                <td><input v-model="form.mnp.ownerName" placeholder="명의자 성명" :readonly="form.mnp.isSameAsCust === 'Y'" /></td>
                <th>{{ isForeignerCustomer ? '외국인 등록번호' : '주민 번호' }}</th>
                <td>
                  <template v-if="isForeignerCustomer">
                    <input
                      :value="form.mnp.ownerSsn1 + (form.mnp.ownerSsn2 ? '-' + form.mnp.ownerSsn2 : '')"
                      @input="setMnpOwnerIdNo($event.target.value)"
                      type="text"
                      placeholder="123456-7890123"
                      maxlength="14"
                      inputmode="numeric"
                      :readonly="form.mnp.isSameAsCust === 'Y'"
                    />
                  </template>
                  <template v-else>
                    <div class="input-split input-ssn">
                      <input v-model="form.mnp.ownerSsn1" type="text" placeholder="앞 6자리" maxlength="6" :readonly="form.mnp.isSameAsCust === 'Y'" />
                      <span>-</span>
                      <input v-model="form.mnp.ownerSsn2" type="text" placeholder="뒤 7자리" maxlength="7" :readonly="form.mnp.isSameAsCust === 'Y'" />
                    </div>
                  </template>
                </td>
              </tr>
              <tr>
                <th>연락처</th>
                <td><input v-model="form.mnp.contactNo" placeholder="연락처" :readonly="form.mnp.isSameAsCust === 'Y'" /></td>
                <th>발급 일자</th>
                <td><input v-model="form.mnp.issueDate" type="date" /></td>
              </tr>
              <tr>
                <th>기존 통신사</th>
                <td>
                  <select v-model="form.mnp.prevCarrier">
                    <option value="">선택</option>
                    <option v-for="c in companyCodes" :key="c.codeValue" :value="c.codeValue">{{ c.codeName }}</option>
                  </select>
                </td>
                <th>메모</th>
                <td><input v-model="form.mnp.mnpMemo" placeholder="번호이동 메모" /></td>
              </tr>
              </tbody>
            </table>
          </section>

          <!-- 7. 비고 사항 -->
          <section class="reg-section">
            <h4 class="reg-section-title">비고 사항</h4>
            <table class="reg-table">
              <tbody>
                <tr>
                  <th>기타 내용</th>
                  <td colspan="3"><textarea v-model="form.customer.remark" rows="5" placeholder="기타 내용" class="remark-textarea"></textarea></td>
                </tr>
              </tbody>
            </table>
          </section>
        </div>
        <div class="modal-reg-footer">
          <button type="button" class="btn-cancel" @click="showCustomerModal = false">취소</button>
          <button v-if="modalMode === 'register'" type="button" class="btn-test-data" @click="fillAllFieldsTestData">테스트 데이터 채우기</button>
          <button type="button" class="btn-save" @click="onSaveCustomer">저장</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, nextTick } from 'vue';
import axios from 'axios';
import * as XLSX from 'xlsx';
import { useAuthStore } from '@/store/auth';
import { AgGridVue } from 'ag-grid-vue3';
import 'ag-grid-community/styles/ag-grid.css';
import 'ag-grid-community/styles/ag-theme-alpine.css';

const gridApi = ref(null);
const rowData = ref([]);
const totalCount = ref(0);
const pageSize = ref(30);

const searchQuery = ref({
  searchCustName: '',
  searchStatus: '',
  searchHpCarrier: '',
  searchPeriodType: '',
  searchCreatedAtFrom: '',
  searchCreatedAtTo: ''
});

// TB_GROUP_CODE / TB_COMMON_CODE 그룹코드 매핑 (공통코드 연동)
// 지급은행: BANK_CODE | 청구서 종류: BILL_TYPE | 요금 감면: DISCOUNT_GB | 지급처: PAY_SOURCE
// 본사 사은품: HEAD_GIFT | 지역 선택: REGION_CODE | 진행 상태(개통상태): CUST_STATUS
// 고객 종류: CUST_TYPE | 고객 인증: CUST_AUTH | 통신사/회사: COMPANY_CODE
const statusCodes = ref([]);     // CUST_STATUS - 진행 상태 (개통상태: 접수, 개통완료 등)
const companyCodes = ref([]);    // COMPANY_CODE - 통신사/회사선택/기존통신사
const bankCodes = ref([]);       // BANK_CODE - 지급은행
const billTypeCodes = ref([]);   // BILL_TYPE - 청구서 종류
const discountTypeCodes = ref([]); // DISCOUNT_GB - 요금 감면
const custAuthCodes = ref([]);   // CUST_AUTH - 고객 인증 (주민발급일자, 면허번호 등)
const paySourceCodes = ref([]);  // PAY_SOURCE - 지급처
const headGiftCodes = ref([]);   // HEAD_GIFT - 본사 사은품
const regionCodes = ref([]);     // REGION_CODE - 지역 선택
const custTypeCodes = ref([]);   // CUST_TYPE - 고객 종류 (개인, 개인사업자, 법인 등)
const userRoleCodes = ref([]);   // USER_ROLE - 사용자 권한

/** axios로 TB_COMMON_CODE 조회 - POST /api/codes/list, body: { groupCode } */
async function loadCodeList(groupCode) {
  if (!groupCode || !groupCode.trim()) return [];
  try {
    const res = await axios.post('/api/codes/list', { groupCode: groupCode.trim() });
    const list = res?.data;
    return Array.isArray(list) ? list : [];
  } catch (err) {
    console.warn('공통코드 조회 실패:', groupCode, err?.response?.status, err?.response?.data);
    return [];
  }
}

/** 페이지/모달용 공통코드 일괄 로드 (axios 호출 10건) */
async function loadCodes() {
  const [
    status, company, bank, billType, discount, custAuth, paySource, headGift, region, custType, userRole
  ] = await Promise.all([
    loadCodeList('CUST_STATUS'),  // 진행 상태 (개통상태)
    loadCodeList('COMPANY_CODE'), // 통신사/회사선택
    loadCodeList('BANK_CODE'),    // 지급은행
    loadCodeList('BILL_TYPE'),    // 청구서 종류
    loadCodeList('DISCOUNT_GB'),  // 요금 감면
    loadCodeList('CUST_AUTH'),   // 고객 인증
    loadCodeList('PAY_SOURCE'),   // 지급처
    loadCodeList('HEAD_GIFT'),    // 본사 사은품
    loadCodeList('REGION_CODE'),  // 지역 선택
    loadCodeList('CUST_TYPE'),    // 고객 종류
    loadCodeList('USER_ROLE')     // 사용자 권한
  ]);
  statusCodes.value = status?.length ? status : [
    { codeValue: 'RECEIPT', codeName: '접수' },
    { codeValue: 'IN_PROGRESS', codeName: '진행중' },
    { codeValue: 'COMPLETED', codeName: '개통완료' },
    { codeValue: 'CANCELLED', codeName: '취소' }
  ];
  // 통신사/회사선택 (COMPANY_CODE) - API 실패 시 DB 기준 폴백
  companyCodes.value = (company?.length ? company : [
    { codeValue: 'SKB', codeName: 'SK브로드밴드' },
    { codeValue: 'KT', codeName: 'KT' },
    { codeValue: 'LGU', codeName: 'LG U+' },
    { codeValue: 'SKT', codeName: 'SKT' },
    { codeValue: 'HELLO', codeName: '헬로' },
    { codeValue: 'RENTAL', codeName: '렌탈' }
  ]);
  bankCodes.value = bank?.length ? bank : [];
  billTypeCodes.value = billType ?? [];
  discountTypeCodes.value = discount ?? [];
  custAuthCodes.value = custAuth?.length ? custAuth : [
    { codeValue: 'ID_DATE', codeName: '주민 발급일자' },
    { codeValue: 'DRV_NO', codeName: '운전면허증 번호' }
  ];
  // 지급처 (PAY_SOURCE) - API 실패 시 폴백
  paySourceCodes.value = (paySource?.length ? paySource : [
    { codeValue: 'NONE', codeName: '사은품 없음' },
    { codeValue: 'AGNT', codeName: '유치자 지급' },
    { codeValue: 'HEAD', codeName: '센터(본사) 지급' }
  ]);
  // 본사사은품 (HEAD_GIFT) - API 실패 시 폴백
  headGiftCodes.value = (headGift?.length ? headGift : [
    { codeValue: 'LOTTE', codeName: '롯데' },
    { codeValue: 'TV', codeName: 'TV' },
    { codeValue: 'EMART_M', codeName: '이마트모바일상품권' },
    { codeValue: 'SHINSEGAE_M', codeName: '신세계모바일상품권' },
    { codeValue: 'HOMEPLUS_M', codeName: '홈플러스모바일상품권' },
    { codeValue: 'NONGHYUP_M', codeName: '농협모바일상품권' },
    { codeValue: 'SKY_KT', codeName: '스카이(KT shop)' }
  ]);
  // 지역 (REGION_CODE) - API 실패 시 폴백
  regionCodes.value = (region?.length ? region : [
    { codeValue: 'SEOUL', codeName: '서울' },
    { codeValue: 'GYEONGGI', codeName: '경기' },
    { codeValue: 'INCHEON', codeName: '인천' },
    { codeValue: 'BUSAN', codeName: '부산' },
    { codeValue: 'DAEGU', codeName: '대구' },
    { codeValue: 'DAEJEON', codeName: '대전' },
    { codeValue: 'GWANGJU', codeName: '광주' },
    { codeValue: 'ULSAN', codeName: '울산' },
    { codeValue: 'SEJONG', codeName: '세종' },
    { codeValue: 'GANGWON', codeName: '강원' },
    { codeValue: 'CHUNGBUK', codeName: '충북' },
    { codeValue: 'CHUNGNAM', codeName: '충남' },
    { codeValue: 'JEONBUK', codeName: '전북' },
    { codeValue: 'JEONNAM', codeName: '전남' },
    { codeValue: 'GYEONGBUK', codeName: '경북' },
    { codeValue: 'GYEONGNAM', codeName: '경남' },
    { codeValue: 'JEJU', codeName: '제주' }
  ]);
  // 고객 종류 (CUST_TYPE) - 협력점은 개인사업자/법인만 허용
  const PARTNER_CUST_TYPES = ['BIZP', 'CORP'];
  custTypeCodes.value = (custType?.length
    ? custType.filter(c => PARTNER_CUST_TYPES.includes(c.codeValue))
    : [
        { codeValue: 'BIZP', codeName: '개인사업자' },
        { codeValue: 'CORP', codeName: '법인' }
      ]
  );
  userRoleCodes.value = userRole ?? [];
}

function formatDate(value) {
  if (!value) return '-';
  const d = typeof value === 'string' ? new Date(value) : value;
  return isNaN(d.getTime()) ? '-' : d.toISOString().slice(0, 10);
}

/** 금액 천 단위 콤마 표시 */
function formatAmount(value) {
  if (value == null || value === '') return '';
  const n = typeof value === 'string' ? parseInt(value.replace(/,/g, ''), 10) : Number(value);
  if (Number.isNaN(n)) return '';
  return n.toLocaleString('ko-KR');
}

/** 입력값에서 숫자만 추출해 숫자로 반환 (금액 입력용) */
function parseAmount(value) {
  if (value == null || value === '') return 0;
  const s = String(value).replace(/[^0-9]/g, '');
  if (!s) return 0;
  const n = parseInt(s, 10);
  return Number.isNaN(n) ? 0 : n;
}

/** 사업자번호 10자리 3-2-5 포맷 (예: 123-45-67890) */
function formatBizNo(value) {
  const s = String(value || '').replace(/[^0-9]/g, '').slice(0, 10);
  if (s.length <= 3) return s;
  if (s.length <= 5) return `${s.slice(0, 3)}-${s.slice(3)}`;
  return `${s.slice(0, 3)}-${s.slice(3, 5)}-${s.slice(5)}`;
}

/** 법인번호 13자리 6-7 포맷 (예: 123456-1234567) */
function formatCorpNo(value) {
  const s = String(value || '').replace(/[^0-9]/g, '').slice(0, 13);
  if (s.length <= 6) return s;
  return `${s.slice(0, 6)}-${s.slice(6)}`;
}

/** 외국인등록번호 13자리 6-7 포맷 */
function formatForeignerRegNo(value) {
  const s = String(value || '').replace(/[^0-9]/g, '').slice(0, 13);
  if (s.length <= 6) return s;
  return `${s.slice(0, 6)}-${s.slice(6)}`;
}

/** 전화/핸드폰 포맷 (010-0000-0000 등) */
function formatPhone(value) {
  const s = String(value || '').replace(/[^0-9]/g, '').slice(0, 11);
  if (s.length <= 3) return s;
  if (s.length <= 7) return `${s.slice(0, 3)}-${s.slice(3)}`;
  return `${s.slice(0, 3)}-${s.slice(3, 7)}-${s.slice(7)}`;
}

/** 계좌번호: 숫자만 허용 (하이픈 없이 저장, maxLength로 제한) */
function formatAccountNo(value) {
  return String(value || '').replace(/[^0-9]/g, '').slice(0, 20);
}

function splitSsn(enc) {
  if (!enc) return ['', ''];
  const parts = enc.includes('-') ? enc.split('-', 2) : [enc.slice(0, 6), enc.slice(6)];
  return [parts[0] || '', parts[1] || ''];
}
function joinSsn(s1, s2) {
  if (!s1 && !s2) return null;
  const a = (s1 || '').trim();
  const b = (s2 || '').trim();
  return a && b ? `${a}-${b}` : (a || b || null);
}

function fullAddress(params) {
  const addr = params.data?.addr || '';
  const detail = params.data?.addrDetail || '';
  if (!addr && !detail) return '-';
  return [addr, detail].filter(Boolean).join(' ');
}

// AG Grid: 헤더(컬럼)는 다 나오게 — 컬럼마다 width/minWidth 유지, sizeColumnsToFit() 미사용 → 가로 스크롤로 전부 표시
const columnDefs = ref([
  { headerName: 'No', valueGetter: 'node?.rowIndex != null ? node.rowIndex + 1 : ""', width: 60, minWidth: 60, suppressFlex: true },
  { field: 'custName', headerName: '고객명', sortable: true, filter: true, flex: 1, minWidth: 90 },
  {
    field: 'receiptDate',
    headerName: '접수일',
    valueFormatter: p => (p.value ? formatDate(p.value) : '-'),
    width: 110,
    minWidth: 100
  },
  { field: 'creatorName', headerName: '등록자', sortable: true, width: 90, minWidth: 80 },
  {
    headerName: '거주지',
    valueFormatter: fullAddress,
    flex: 1,
    minWidth: 120,
    tooltipField: 'addr'
  },
  { field: 'hpCarrierName', headerName: '통신사', sortable: true, width: 80, minWidth: 70, valueFormatter: p => p.value ?? p.data?.hpCarrier ?? '' },
  { field: 'prodName', headerName: '상품명', width: 100, minWidth: 80, valueFormatter: p => p.value ?? '-' },
  { field: 'prodType', headerName: '유형', width: 80, minWidth: 60, valueFormatter: p => p.value ?? '-' },
  { field: 'setInfo', headerName: '세트', width: 80, minWidth: 60, valueFormatter: p => p.value ?? '-' },
  { field: 'subscriptionNo', headerName: '가입번호', width: 100, minWidth: 90, valueGetter: params => params.data?.subscriptionNo ?? '', valueFormatter: p => (p.value != null && String(p.value).trim() !== '' ? p.value : '-') },
  { field: 'partner', headerName: '협력사', width: 90, minWidth: 70, valueFormatter: () => '더원컴퍼니' },
  { field: 'acquirer', headerName: '유치자', width: 90, minWidth: 70, valueFormatter: () => '더원컴퍼니' },
  { field: 'openDate', headerName: '개통일', width: 110, minWidth: 100, valueFormatter: p => (p.value ? formatDate(p.value) : '-') },
  {
    field: 'statusName',
    headerName: '개통상태',
    width: 90,
    minWidth: 80,
    valueFormatter: p => p.value ?? p.data?.status ?? '',
    cellClass: params => {
      const v = (params.value || params.data?.status || '').toString();
      if (v === '개통완료' || v === 'COMPLETED') return 'status-done';
      if (v === '취소' || v === 'CANCELLED') return 'status-cancel';
      return '';
    }
  },
  { field: 'gift', headerName: '사은품', width: 80, minWidth: 70, valueFormatter: p => p.value ?? '-' },
  { field: 'amount', headerName: '금액', width: 90, minWidth: 70, valueFormatter: p => (p.value != null && p.value !== '' ? formatAmount(p.value) : '-') },
  { field: 'addGift', headerName: '추가사은품', width: 90, minWidth: 80, valueFormatter: p => (p.value != null && p.value !== '' ? formatAmount(p.value) : '-') },
  { field: 'paySource', headerName: '지급처', width: 80, minWidth: 70, valueFormatter: p => p.value ?? '-' },
  { field: 'payDone', headerName: '지급완료', width: 80, minWidth: 80, valueFormatter: p => (p.value ? formatDate(p.value) : '-') },
  { field: 'contractPeriod', headerName: '약정', width: 90, minWidth: 70, valueFormatter: p => p.value ?? '-' },
  { field: 'remark', headerName: '최종상담내용', flex: 1, minWidth: 120 },
  {
    colId: 'manage',
    headerName: '관리',
    width: 80,
    minWidth: 70,
    suppressFlex: true,
    cellRenderer: params => {
      const container = document.createElement('div');
      container.style.display = 'flex';
      container.style.justifyContent = 'center';

      const delBtn = document.createElement('button');
      delBtn.innerText = '삭제';
      delBtn.className = 'btn-table-del';
      delBtn.onclick = () => onDelete(params.data);

      container.appendChild(delBtn);
      return container;
    }
  }
]);

const defaultColDef = { resizable: true, sortable: true };

const onGridReady = params => {
  gridApi.value = params.api;
  // 헤더는 다 나오게: sizeColumnsToFit() 사용 안 함 → 컬럼 너비 유지, 가로 스크롤로 전부 표시
};

const onSearch = async () => {
  try {
    const payload = { ...searchQuery.value, custKind: 'PARTNER' };
    const res = await axios.post('/api/customers/list', payload);
    rowData.value = Array.isArray(res.data) ? res.data : [];
    totalCount.value = rowData.value.length;
  } catch (err) {
    console.error('고객 목록 조회 오류:', err);
    rowData.value = [];
    totalCount.value = 0;
  }
};

const resetSearch = () => {
  searchQuery.value = {
    searchCustName: '',
    searchStatus: '',
    searchHpCarrier: '',
    searchPeriodType: '',
    searchCreatedAtFrom: '',
    searchCreatedAtTo: ''
  };
  onSearch();
};

const onPageSizeChange = () => {
  if (gridApi.value && typeof gridApi.value.setGridOption === 'function') {
    gridApi.value.setGridOption('paginationPageSize', pageSize.value);
  }
};

const EXCEL_ROW_OPTIONS = [50, 100, 150, 200, 250, 300];
const showExcelModal = ref(false);
const excelRowCount = ref(50);
const excelDownloading = ref(false);
const excelSearchPeriodType = ref('');
const excelSearchDateFrom = ref('');
const excelSearchDateTo = ref('');

const onExcelSave = () => {
  excelRowCount.value = 50;
  excelSearchPeriodType.value = searchQuery.value.searchPeriodType || '';
  excelSearchDateFrom.value = searchQuery.value.searchCreatedAtFrom || '';
  excelSearchDateTo.value = searchQuery.value.searchCreatedAtTo || '';
  showExcelModal.value = true;
};

function closeExcelModal() {
  showExcelModal.value = false;
}

/** 엑셀 저장용: 예시 양식 22컬럼(No, 계약번호, 가입일, 만기일, 성명, 주민등록번호, …)에 맞춰 매핑 */
function toListDisplayRow(row, index) {
  const joinDate = row.createdAt ? formatDate(row.createdAt) : '';
  const maturityDate = computedMaturityDate(row.openDate, row.contractPeriod) || '';
  return {
    'No.': index + 1,
    '계약번호': row.custId ?? row.subscriptionNo ?? '',
    '가입일': joinDate,
    '만기일': maturityDate,
    '성명': row.custName ?? '',
    '주민등록번호': '', // 목록에는 미포함(개인정보)
    '사업자번호': row.bizNo ?? '',
    '휴대폰': row.hpNo ?? '',
    '전화번호': row.telNo ?? '',
    '이메일': row.email ?? '',
    '주소': row.addr ?? '',
    '상세주소': row.addrDetail ?? '',
    '상품코드': row.subscriptionNo ?? '',
    '상품명': row.prodName ?? '',
    '유지보험료': formatAmount(row.amount) || '',
    '납기일': '',
    '보험기간': row.contractPeriod ?? '',
    '고객유형': row.custTypeName ?? row.custType ?? '',
    '모집인명': row.counselorName ?? row.creatorName ?? '',
    '모집인코드': row.assignedUserId ?? '',
    '계약상태': row.statusName ?? row.status ?? '',
    '비고': row.remark ?? ''
  };
}

async function doExcelDownload() {
  excelDownloading.value = true;
  try {
    const payload = {
      ...searchQuery.value,
      searchPeriodType: excelSearchPeriodType.value || undefined,
      searchCreatedAtFrom: excelSearchDateFrom.value || undefined,
      searchCreatedAtTo: excelSearchDateTo.value || undefined,
      limit: excelRowCount.value
    };
    const res = await axios.post('/api/customers/list', payload);
    const rows = Array.isArray(res.data) ? res.data : [];
    const excelData = rows.map((row, idx) => toListDisplayRow(row, idx));
    const ws = XLSX.utils.json_to_sheet(excelData);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, '고객상품');
    const ymd = new Date().toISOString().slice(0, 10).replace(/-/g, '');
    const fileName = `고객상품(${ymd}).xlsx`;
    const buf = XLSX.write(wb, { bookType: 'xlsx', type: 'array' });
    const blob = new Blob([buf], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = fileName;
    a.style.display = 'none';
    document.body.appendChild(a);
    a.click();
    setTimeout(() => {
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
    }, 1500);
    closeExcelModal();
  } catch (err) {
    console.error('엑셀 다운로드 오류:', err);
    alert(err.response?.data?.message || '엑셀 다운로드 중 오류가 발생했습니다.');
  } finally {
    excelDownloading.value = false;
  }
}

// 고객 등록 모달
const showCustomerModal = ref(false);
const modalMode = ref('register'); // 'register' | 'detail'
const counselorOptions = ref([]);
const selectedCounselorUserId = ref(null);
// 상품 유형: 단독(S0), DPS(D0), TPS(T0) 만 사용 (TB_COMMON_CODE PROD_COMB_GB 기준)
const productTypeOptions = [{ codeValue: 'S0', codeName: '단독' }, { codeValue: 'D0', codeName: 'DPS' }, { codeValue: 'T0', codeName: 'TPS' }];
const productType = ref('S0');
const productRows = ref([createEmptyProductRow()]);

// 비고 textarea 미리 채울 기본 문구 (체크박스 없이 라벨만)
const DEFAULT_REMARK_TEXT = `진행 통신사 :
상품 :
결합 :
필요금 (결합전) :
설치비 :
사은품 :`;

function getEmptyCustomer() {
  return {
    custName: '',
    repName: '',
    custType: 'PERS',
    status: 'RECEIPT',
    joinDate: '',
    receiptDate: '',
    cancellationDate: '',
    ssn1: '',
    ssn2: '',
    bizNo: '',
    corpNo: '',
    foreignerRegNo: '',
    zipCode: '',
    addr: '',
    addrDetail: '',
    telNo: '',
    hpNo: '',
    hpCarrier: '',
    email: '',
    custAuthType: '',
    custAuthVal: '',
    counselorName: '',
    deptId: null,
    assignedUserId: null,
    remark: DEFAULT_REMARK_TEXT,
    voucherReturnYn: 'N'
  };
}

function getEmptyPayment() {
  return {
    payMethod: 'BANK',
    billType: '',
    discountType: '',
    accountHolder: '',
    bankCardName: '',
    accountCardNo: '',
    holderSsn1: '',
    holderSsn2: '',
    relationWithCust: '',
    isSameAsCust: 'N',
    remark: ''
  };
}

function getEmptyGift() {
  return {
    giftName: '',
    giftAmount: 0,
    addGiftAmount: 0,
    paySchedDate: '',
    payDoneDate: '',
    paySource: '',
    bankName: '',
    accountNo: '',
    accountHolder: '',
    holderSsn1: '',
    holderSsn2: '',
    isSameAsBank: 'N',
    remark: ''
  };
}

function getEmptyMnp() {
  return {
    isSameAsCust: 'N',
    mnpTelNo: '',
    ownerName: '',
    ownerSsn1: '',
    ownerSsn2: '',
    contactNo: '',
    issueDate: '',
    prevCarrier: '',
    mnpMemo: '',
    remark: ''
  };
}

const form = ref({
  customer: getEmptyCustomer(),
  payment: getEmptyPayment(),
  gift: getEmptyGift(),
  headGift: getEmptyGift(),
  mnp: getEmptyMnp()
});

// 동일 정보 적용 체크박스 (납부: 고객→납부, 사은품: 납부→사은품, 번호이동: 고객→번호이동)
const sameAsCustomerForPayment = ref(false);
const sameAsPaymentForGift = ref(false);

// 고객 종류(custType)별 식별번호 필드 노출: 협력점은 BIZP=사업자번호, CORP=법인번호+사업자번호 (주민번호/외국인번호 없음)
const showSsn = computed(() => false); // 협력점 고객은 주민번호 불필요
const showBizNo = computed(() => {
  const t = form.value.customer?.custType || '';
  return ['BIZP', 'CORP'].includes(t);
});
const showCorpNo = computed(() => {
  const t = form.value.customer?.custType || '';
  return t === 'CORP';
});
const showForeignerNo = computed(() => false); // 협력점 고객은 외국인등록번호 불필요

/** 협력점 고객은 외국인 타입 없음 */
const isForeignerCustomer = computed(() => false);

function setPaymentHolderIdNo(val) {
  const formatted = formatForeignerRegNo(val);
  const parts = formatted.split('-');
  if (form.value.payment) {
    form.value.payment.holderSsn1 = parts[0] || '';
    form.value.payment.holderSsn2 = parts[1] || '';
  }
}

function setGiftHolderIdNo(val) {
  const formatted = formatForeignerRegNo(val);
  const parts = formatted.split('-');
  if (form.value.gift) {
    form.value.gift.holderSsn1 = parts[0] || '';
    form.value.gift.holderSsn2 = parts[1] || '';
  }
}

function setMnpOwnerIdNo(val) {
  const formatted = formatForeignerRegNo(val);
  const parts = formatted.split('-');
  if (form.value.mnp) {
    form.value.mnp.ownerSsn1 = parts[0] || '';
    form.value.mnp.ownerSsn2 = parts[1] || '';
  }
}

function copyCustomerToPayment() {
  const c = form.value.customer;
  const p = form.value.payment;
  if (!c || !p) return;
  p.accountHolder = c.custName ?? '';
  if (c.custType === 'FORN') {
    const parts = (c.foreignerRegNo || '').split('-');
    p.holderSsn1 = parts[0]?.trim() ?? '';
    p.holderSsn2 = parts[1]?.trim() ?? '';
  } else {
    p.holderSsn1 = c.ssn1 ?? '';
    p.holderSsn2 = c.ssn2 ?? '';
  }
}

function copyPaymentToGift() {
  const pay = form.value.payment;
  const g = form.value.gift;
  if (pay && g) {
    g.bankName = pay.bankCardName ?? '';
    g.accountNo = pay.accountCardNo ?? '';
    g.accountHolder = pay.accountHolder ?? '';
    g.holderSsn1 = pay.holderSsn1 ?? '';
    g.holderSsn2 = pay.holderSsn2 ?? '';
  }
}

function copyCustomerToMnp() {
  const c = form.value.customer;
  const m = form.value.mnp;
  if (!c || !m) return;
  m.ownerName = c.custName ?? '';
  m.mnpTelNo = c.telNo ?? '';
  m.contactNo = c.hpNo ?? '';
  if (c.custType === 'FORN') {
    const parts = (c.foreignerRegNo || '').split('-');
    m.ownerSsn1 = parts[0]?.trim() ?? '';
    m.ownerSsn2 = parts[1]?.trim() ?? '';
  } else {
    m.ownerSsn1 = c.ssn1 ?? '';
    m.ownerSsn2 = c.ssn2 ?? '';
  }
}

watch(sameAsCustomerForPayment, (checked) => {
  if (checked) copyCustomerToPayment();
});
watch(() => form.value.customer, () => {
  if (sameAsCustomerForPayment.value) copyCustomerToPayment();
}, { deep: true });

watch(sameAsPaymentForGift, (checked) => {
  if (checked) copyPaymentToGift();
});
watch(() => form.value.payment, () => {
  if (sameAsPaymentForGift.value) copyPaymentToGift();
}, { deep: true });

watch(() => form.value.mnp?.isSameAsCust, (val) => {
  if (val === 'Y') copyCustomerToMnp();
});
watch(() => form.value.customer, () => {
  if (form.value.mnp?.isSameAsCust === 'Y') copyCustomerToMnp();
}, { deep: true });

function createEmptyProductRow() {
  return {
    company: '',
    region: '',
    subscriptionNo: '',
    product: '',
    productOption: '',
    contract: '',
    openStatus: '',
    openDate: '',
    cancelDate: '',
    setTop: '',
    vas: '',
    lineCount: 1
  };
}

/** 약정 문자열에서 개월 수 추출 (예: "24", "24개월", "20년" -> 24 or 240) */
function parseContractMonths(contractStr) {
  if (!contractStr || typeof contractStr !== 'string') return null;
  const s = contractStr.trim();
  const numMatch = s.match(/^(\d+)/);
  if (!numMatch) return null;
  const num = parseInt(numMatch[1], 10);
  if (Number.isNaN(num) || num <= 0) return null;
  if (/년/.test(s)) return num * 12;
  return num;
}

/** 만기일 계산: 개통일자 + 약정 개월 (DB 저장 없음, 화면 표시용) */
function computedMaturityDate(openDateVal, contractVal) {
  const openStr = (openDateVal || '').toString().trim();
  if (!openStr) return '';
  const months = parseContractMonths(contractVal);
  if (months == null) return '';
  try {
    const d = new Date(openStr + 'T00:00:00');
    if (isNaN(d.getTime())) return '';
    d.setMonth(d.getMonth() + months);
    return d.toISOString().slice(0, 10);
  } catch {
    return '';
  }
}

function getRequiredRowCount() {
  if (productType.value === 'S0') return 1;
  if (productType.value === 'D0') return 2;
  if (productType.value === 'T0') return 3;
  return 1;
}

function onProductTypeChange() {
  const required = getRequiredRowCount();
  const current = productRows.value;
  if (current.length < required) {
    const add = required - current.length;
    for (let i = 0; i < add; i++) productRows.value.push(createEmptyProductRow());
  } else if (current.length > required) {
    productRows.value = current.slice(0, required);
  }
}

function onCounselorChange() {
  const uid = selectedCounselorUserId.value;
  const u = counselorOptions.value.find(o => o.userId === uid);
  form.value.customer.counselorName = u ? (u.userName || '') : '';
  form.value.customer.deptId = u ? (u.deptId ?? null) : null;
  form.value.customer.assignedUserId = u ? (u.userId ?? null) : null;
}

async function loadCounselorOptions() {
  try {
    const res = await axios.get('/api/customers/users/options');
    counselorOptions.value = Array.isArray(res.data) ? res.data : [];
  } catch (err) {
    console.warn('상담원 목록 조회 실패:', err);
    counselorOptions.value = [];
  }
}

async function openCustomerModal() {
  await loadCodes();
  await loadCounselorOptions();
  modalMode.value = 'register';
  form.value = {
    customer: getEmptyCustomer(),
    payment: getEmptyPayment(),
    gift: getEmptyGift(),
    headGift: getEmptyGift(),
    mnp: getEmptyMnp()
  };
  productType.value = 'S0';
  productRows.value = [createEmptyProductRow()];
  sameAsCustomerForPayment.value = false;
  sameAsPaymentForGift.value = false;
  const auth = useAuthStore();
  const me = auth.user;
  if (me?.userId) {
    selectedCounselorUserId.value = me.userId;
    form.value.customer.counselorName = me.userName || '';
    form.value.customer.deptId = me.deptId ?? null;
    form.value.customer.assignedUserId = me.userId ?? null;
  } else {
    selectedCounselorUserId.value = counselorOptions.value[0]?.userId ?? null;
    onCounselorChange();
  }
  showCustomerModal.value = true;
}

async function openDetailModal(custId) {
  if (!custId) return;
  await loadCodes();
  await loadCounselorOptions();
  try {
    const res = await axios.get('/api/customers/detail', { params: { custId } });
    const detail = res.data;
    if (!detail) {
      alert('고객 정보를 찾을 수 없습니다.');
      return;
    }
    modalMode.value = 'detail';
    mapDetailToForm(detail);
    sameAsCustomerForPayment.value = false;
    sameAsPaymentForGift.value = false;
    await nextTick();
    showCustomerModal.value = true;
  } catch (err) {
    console.error('고객 상세 조회 오류:', err);
    alert(err.response?.data?.message || '고객 정보를 불러오는 중 오류가 발생했습니다.');
  }
}

function mapDetailToForm(detail) {
  const c = detail.customer || {};
  const [ssn1 = '', ssn2 = ''] = (c.ssnEnc || '').includes('-') ? c.ssnEnc.split('-', 2) : [c.ssnEnc?.slice(0, 6) || '', c.ssnEnc?.slice(6) || ''];
  const joinDateStr = c.joinDate ? (typeof c.joinDate === 'string' ? c.joinDate.slice(0, 10) : c.joinDate) : '';
  const receiptDateStr = c.receiptDate ? (typeof c.receiptDate === 'string' ? c.receiptDate.slice(0, 10) : c.receiptDate) : '';
  const cancelDateStr = c.cancellationDate ? (typeof c.cancellationDate === 'string' ? c.cancellationDate.slice(0, 10) : c.cancellationDate) : '';
  form.value.customer = {
    custId: c.custId,
    custName: c.custName || '',
    repName: c.repName || '',
    custType: c.custType || 'PERS',
    status: c.status || 'RECEIPT',
    joinDate: joinDateStr,
    receiptDate: receiptDateStr,
    cancellationDate: cancelDateStr,
    ssn1, ssn2,
    bizNo: formatBizNo(c.bizNo || ''),
    corpNo: formatCorpNo(c.corpNo || ''),
    foreignerRegNo: formatForeignerRegNo(c.foreignerRegNo || ''),
    zipCode: c.zipCode || '',
    addr: c.addr || '',
    addrDetail: c.addrDetail || '',
    telNo: formatPhone(c.telNo || ''),
    hpNo: formatPhone(c.hpNo || ''),
    hpCarrier: c.hpCarrier || '',
    email: c.email || '',
    custAuthType: c.custAuthType || '',
    custAuthVal: c.custAuthVal || '',
    counselorName: c.counselorName || '',
    deptId: c.deptId ?? null,
    assignedUserId: c.assignedUserId ?? null,
    remark: c.remark || '',
    voucherReturnYn: c.voucherReturnYn || 'N'
  };
  selectedCounselorUserId.value = c.assignedUserId ?? null;
  const pay = detail.payment || {};
  const [paySsn1, paySsn2] = splitSsn(pay.holderSsnEnc);
  form.value.payment = {
    payMethod: pay.payMethod || 'BANK',
    billType: pay.billType || '',
    discountType: pay.discountType || '',
    accountHolder: pay.accountHolder || '',
    bankCardName: pay.bankCardName || '',
    accountCardNo: pay.accountCardNo || '',
    holderSsn1: paySsn1,
    holderSsn2: paySsn2,
    relationWithCust: pay.relationWithCust || '',
    isSameAsCust: pay.isSameAsCust || 'N',
    remark: pay.remark || ''
  };
  const gifts = detail.gifts || [];
  const generalGift = gifts.find(g => g.giftGb === 'GENERAL') || {};
  const headGift = gifts.find(g => g.giftGb === 'HEAD') || {};
  const [giftSsn1, giftSsn2] = splitSsn(generalGift.holderSsnEnc);
  const [headSsn1, headSsn2] = splitSsn(headGift.holderSsnEnc);
  const giftPaySchedStr = generalGift.paySchedDate ? (typeof generalGift.paySchedDate === 'string' ? generalGift.paySchedDate.slice(0, 10) : String(generalGift.paySchedDate).slice(0, 10)) : '';
  const giftPayDoneStr = generalGift.payDoneDate ? (typeof generalGift.payDoneDate === 'string' ? generalGift.payDoneDate.slice(0, 10) : String(generalGift.payDoneDate).slice(0, 10)) : '';
  form.value.gift = {
    giftName: generalGift.giftName || '',
    giftAmount: generalGift.giftAmount ?? 0,
    addGiftAmount: generalGift.addGiftAmount ?? 0,
    paySchedDate: giftPaySchedStr,
    payDoneDate: giftPayDoneStr,
    paySource: generalGift.paySource || '',
    bankName: generalGift.bankName || '',
    accountNo: generalGift.accountNo || '',
    accountHolder: generalGift.accountHolder || '',
    holderSsn1: giftSsn1,
    holderSsn2: giftSsn2,
    isSameAsBank: generalGift.isSameAsBank || 'N',
    remark: generalGift.remark || ''
  };
  const headPayDoneStr = headGift.payDoneDate ? (typeof headGift.payDoneDate === 'string' ? headGift.payDoneDate.slice(0, 10) : headGift.payDoneDate) : '';
  const headPaySchedStr = headGift.paySchedDate ? (typeof headGift.paySchedDate === 'string' ? headGift.paySchedDate.slice(0, 10) : headGift.paySchedDate) : '';
  form.value.headGift = {
    giftName: headGift.giftName || '',
    giftAmount: headGift.giftAmount ?? 0,
    addGiftAmount: headGift.addGiftAmount ?? 0,
    paySchedDate: headPaySchedStr,
    payDoneDate: headPayDoneStr,
    paySource: headGift.paySource || '',
    bankName: headGift.bankName || '',
    accountNo: headGift.accountNo || '',
    accountHolder: headGift.accountHolder || '',
    holderSsn1: headSsn1,
    holderSsn2: headSsn2,
    isSameAsBank: headGift.isSameAsBank || 'N',
    remark: headGift.remark || ''
  };
  const products = detail.products || [];
  if (products.length > 0) {
    const raw = (products[0].prodGb || '').trim().toUpperCase();
    if (raw === 'D0' || raw === 'DPS') productType.value = 'D0';
    else if (raw === 'T0' || raw === 'TPS') productType.value = 'T0';
    else productType.value = 'S0';
    const required = getRequiredRowCount();
    productRows.value = products.slice(0, required).map(p => {
      const openVal = p.openDate;
      const cancelVal = p.cancelDate;
      const openStr = openVal ? (typeof openVal === 'string' ? openVal.slice(0, 10) : openVal) : '';
      const cancelStr = cancelVal ? (typeof cancelVal === 'string' ? cancelVal.slice(0, 10) : cancelVal) : '';
      return {
        company: p.compName || '',
        region: p.regionName || '',
        subscriptionNo: p.subscriptionNo || p.subscription_no || '',
        product: p.prodName || '',
        productOption: p.prodOpt || '',
        contract: p.contractPeriod || '',
        openStatus: p.openStatus || p.open_status || '',
        openDate: openStr,
        cancelDate: cancelStr,
        setTop: p.stbType || '',
        vas: p.vasName || '',
        lineCount: p.lineCount ?? 1
      };
    });
    while (productRows.value.length < required) productRows.value.push(createEmptyProductRow());
  } else {
    productType.value = 'S0';
    productRows.value = [createEmptyProductRow()];
  }
  const mnp0 = (detail.mnps && detail.mnps[0]) || {};
  const [mnpSsn1, mnpSsn2] = splitSsn(mnp0.ownerSsnEnc);
  form.value.mnp = {
    isSameAsCust: mnp0.isSameAsCust || 'N',
    mnpTelNo: mnp0.mnpTelNo || '',
    ownerName: mnp0.ownerName || '',
    ownerSsn1: mnpSsn1,
    ownerSsn2: mnpSsn2,
    contactNo: mnp0.contactNo || '',
    issueDate: mnp0.issueDate || '',
    prevCarrier: mnp0.prevCarrier || '',
    mnpMemo: mnp0.mnpMemo || '',
    remark: mnp0.remark || ''
  };
}

function onRowClicked(params) {
  if (!params.data?.custId) return;
  if (params.column?.getColId() === 'manage' || params.event?.target?.closest('button')) return;
  openDetailModal(params.data.custId);
}

function copySsn() {
  const s1 = form.value.customer.ssn1 || '';
  const s2 = form.value.customer.ssn2 || '';
  if (!s1 || !s2) return;
  const text = s1 + '-' + s2;
  navigator.clipboard.writeText(text).then(() => alert('주민번호가 복사되었습니다.')).catch(() => alert('복사 실패'));
}

function onSearchAddress() {
  if (typeof window.daum === 'undefined' || !window.daum.Postcode) {
    alert('주소 검색 스크립트를 불러오는 중입니다. 잠시 후 다시 시도해주세요.');
    return;
  }
  const theme = { bgColor: '#fff', searchBgColor: '#1976d2', queryTextColor: '#333' };
  new window.daum.Postcode({
    theme,
    oncomplete: function (data) {
      form.value.customer.zipCode = data.zonecode || '';
      form.value.customer.addr = (data.userSelectedType === 'R' ? data.roadAddress : data.jibunAddress) || data.roadAddress || data.jibunAddress || '';
      form.value.customer.addrDetail = data.buildingName ? data.buildingName : '';
    }
  }).open();
}

function buildRegisterPayload() {
  const c = form.value.customer;
  const ssnEnc = (c.ssn1 && c.ssn2) ? c.ssn1 + '-' + c.ssn2 : null;
  const auth = useAuthStore();
  const customerPayload = {
    custName: c.custName,
    repName: c.repName || null,
    custType: c.custType || null,
    custKind: 'PARTNER',
    ssnEnc,
    bizNo: c.bizNo || null,
    corpNo: c.corpNo || null,
    foreignerRegNo: (c.foreignerRegNo || '').trim() || null,
    zipCode: c.zipCode || null,
    addr: c.addr || null,
    addrDetail: c.addrDetail || null,
    telNo: c.telNo || null,
    hpNo: c.hpNo || null,
    hpCarrier: c.hpCarrier || null,
    email: c.email || null,
    custAuthType: c.custAuthType || null,
    custAuthVal: c.custAuthVal || null,
    counselorName: c.counselorName || null,
    deptId: c.deptId ?? null,
    assignedUserId: c.assignedUserId ?? null,
    remark: (c.remark || '').trim() || null,
    status: c.status || 'RECEIPT',
    openDate: null,
    joinDate: (c.joinDate || '').toString().trim() || null,
    receiptDate: (c.receiptDate || '').toString().trim() || null,
    cancellationDate: (c.cancellationDate || '').toString().trim() || null,
    voucherReturnYn: (c.voucherReturnYn === 'Y' ? 'Y' : 'N')
  };
  if (auth.user?.userId) customerPayload.creatorId = auth.user.userId;
  if (modalMode.value === 'detail' && c.custId) customerPayload.custId = c.custId;
  const pay = form.value.payment;
  const giftForm = form.value.gift;
  const headForm = form.value.headGift;
  const mnpForm = form.value.mnp;
  return {
    customer: customerPayload,
    voucherReturnYn: customerPayload.voucherReturnYn,
    payment: {
      ...pay,
      holderSsnEnc: joinSsn(pay.holderSsn1, pay.holderSsn2) || null
    },
    gift: {
      ...giftForm,
      giftGb: 'GENERAL',
      holderSsnEnc: joinSsn(giftForm.holderSsn1, giftForm.holderSsn2) || null
    },
    headGift: {
      ...headForm,
      giftGb: 'HEAD',
      holderSsnEnc: joinSsn(headForm.holderSsn1, headForm.holderSsn2) || null
    },
    products: productRows.value
      .filter(r => ((r.company || '').trim() || (r.product || '').trim() || (r.subscriptionNo || '').trim()))
      .map(r => ({
        compName: r.company || null,
        prodGb: productType.value,
        regionName: r.region || null,
        subscriptionNo: (r.subscriptionNo || '').trim() || null,
        prodName: r.product || null,
        prodOpt: r.productOption || null,
        contractPeriod: r.contract || null,
        openStatus: (r.openStatus || '').trim() || null,
        openDate: (r.openDate || '').toString().trim() || null,
        cancelDate: (r.cancelDate || '').toString().trim() || null,
        stbType: r.setTop || null,
        vasName: r.vas || null,
        lineCount: r.lineCount || 1
      })),
    mnp: {
      ...mnpForm,
      ownerSsnEnc: joinSsn(mnpForm.ownerSsn1, mnpForm.ownerSsn2) || null
    }
  };
}

/** 협력점 고객 종류별 식별번호 필수 검증. 검증 실패 시 메시지 반환, 성공 시 null */
function validateCustomerIdByType() {
  const c = form.value.customer;
  const type = (c.custType || 'BIZP').trim();
  const hasBizNo = !!(c.bizNo?.trim());
  const hasCorpNo = !!(c.corpNo?.trim());

  if (type === 'BIZP') {
    if (!hasBizNo) return '고객 종류가 개인사업자일 때 사업자번호는 필수입니다.';
  } else if (type === 'CORP') {
    if (!hasCorpNo) return '고객 종류가 법인일 때 법인번호는 필수입니다.';
  }
  return null;
}

/** 숫자만 추출 (하이픈 등 제거) */
function digitsOnly(val) {
  return String(val || '').replace(/[^0-9]/g, '');
}

/** 협력점 고객 저장 시 형식 발리데이션. 실패 시 메시지 반환, 성공 시 null */
function validateFormats() {
  const c = form.value.customer;
  const type = (c.custType || 'BIZP').trim();

  // 사업자번호 10자리 (3-2-5)
  if (['BIZP', 'CORP'].includes(type) && (c.bizNo || '').trim()) {
    const biz = digitsOnly(c.bizNo);
    if (biz.length !== 10) return '사업자번호는 숫자 10자리(000-00-00000 형식)로 입력해주세요.';
  }

  // 법인번호 13자리 (6-7)
  if (type === 'CORP' && (c.corpNo || '').trim()) {
    const corp = digitsOnly(c.corpNo);
    if (corp.length !== 13) return '법인번호는 숫자 13자리(123456-1234567 형식)로 입력해주세요.';
  }

  // 전화번호: 9~11자리 숫자 (02-XXX-XXXX, 010-XXXX-XXXX 등)
  if ((c.telNo || '').trim()) {
    const tel = digitsOnly(c.telNo);
    if (tel.length < 9 || tel.length > 11) return '전화번호 형식이 올바르지 않습니다. (예: 010-0000-0000, 02-000-0000)';
  }

  // 핸드폰: 9~11자리 숫자
  if ((c.hpNo || '').trim()) {
    const hp = digitsOnly(c.hpNo);
    if (hp.length < 9 || hp.length > 11) return '핸드폰 번호 형식이 올바르지 않습니다. (예: 010-0000-0000)';
  }

  // 납부 정보 - 주민번호 (협력점 담당자 주민번호, 선택)
  const pay = form.value.payment;
  if (pay && (pay.holderSsn1 || pay.holderSsn2)) {
    if (!/^\d{6}$/.test(digitsOnly(pay.holderSsn1))) return '납부 정보의 주민번호 앞 6자리를 숫자로 입력해주세요.';
    if (!/^\d{7}$/.test(digitsOnly(pay.holderSsn2))) return '납부 정보의 주민번호 뒤 7자리를 숫자로 입력해주세요.';
  }

  // 사은품 정보 - 주민번호 (선택)
  const gift = form.value.gift;
  if (gift && (gift.holderSsn1 || gift.holderSsn2)) {
    if (!/^\d{6}$/.test(digitsOnly(gift.holderSsn1))) return '사은품 정보의 주민번호 앞 6자리를 숫자로 입력해주세요.';
    if (!/^\d{7}$/.test(digitsOnly(gift.holderSsn2))) return '사은품 정보의 주민번호 뒤 7자리를 숫자로 입력해주세요.';
  }

  // 본사 사은품 - 주민번호 (선택)
  const headGift = form.value.headGift;
  if (headGift && (headGift.holderSsn1 || headGift.holderSsn2)) {
    if (!/^\d{6}$/.test(digitsOnly(headGift.holderSsn1))) return '본사 사은품의 주민번호 앞 6자리를 숫자로 입력해주세요.';
    if (!/^\d{7}$/.test(digitsOnly(headGift.holderSsn2))) return '본사 사은품의 주민번호 뒤 7자리를 숫자로 입력해주세요.';
  }

  // 번호이동 - 명의자 주민번호 (선택)
  const mnp = form.value.mnp;
  if (mnp && (mnp.ownerSsn1 || mnp.ownerSsn2)) {
    if (!/^\d{6}$/.test(digitsOnly(mnp.ownerSsn1))) return '번호이동 주민번호 앞 6자리를 숫자로 입력해주세요.';
    if (!/^\d{7}$/.test(digitsOnly(mnp.ownerSsn2))) return '번호이동 주민번호 뒤 7자리를 숫자로 입력해주세요.';
  }

  // 계좌번호: 숫자만, 1~20자리 (입력된 경우만)
  if (pay?.accountCardNo?.trim()) {
    const d = digitsOnly(pay.accountCardNo);
    if (d.length < 1 || d.length > 20) return '납부 정보 계좌번호는 숫자 1~20자리로 입력해주세요.';
  }
  if (gift?.accountNo?.trim()) {
    const d = digitsOnly(gift.accountNo);
    if (d.length < 1 || d.length > 20) return '사은품 계좌번호는 숫자 1~20자리로 입력해주세요.';
  }

  return null;
}

/** 고객 등록 폼 전체 항목 테스트용 샘플 데이터 채우기 (협력점 고객 기준) */
function fillAllFieldsTestData() {
  const c = form.value.customer;
  c.custName = '테스트협력점';
  c.repName = '홍길동';
  c.custType = 'BIZP';
  c.status = 'RECEIPT';
  c.joinDate = '2025-01-20';
  c.receiptDate = '2025-01-15';
  c.cancellationDate = '';
  c.ssn1 = '';
  c.ssn2 = '';
  c.bizNo = '123-45-67890';
  c.corpNo = '';
  c.foreignerRegNo = '';
  c.zipCode = '06134';
  c.addr = '서울 강남구 테헤란로 123';
  c.addrDetail = '101동 1001호';
  c.telNo = '02-1234-5678';
  c.hpNo = '010-1234-5678';
  c.hpCarrier = 'SKT';
  c.email = 'test@example.com';
  c.custAuthType = 'PHONE';
  c.custAuthVal = '01012345678';
  c.remark = '전체항목 테스트 비고';
  c.voucherReturnYn = 'Y';

  const pay = form.value.payment;
  pay.payMethod = 'BANK';
  pay.billType = '';
  pay.discountType = '';
  pay.accountHolder = '전체항목테스트고객';
  pay.bankCardName = '국민은행';
  pay.accountCardNo = '12345678901234';
  pay.holderSsn1 = '900101';
  pay.holderSsn2 = '1234567';
  pay.relationWithCust = '본인';
  pay.isSameAsCust = 'Y';
  pay.remark = '납부 테스트 비고';

  const gift = form.value.gift;
  gift.giftName = '사은품A';
  gift.giftAmount = 100000;
  gift.addGiftAmount = 50000;
  gift.paySchedDate = '2025-02-15';
  gift.payDoneDate = '2025-02-20';
  gift.paySource = '';
  gift.bankName = '국민은행';
  gift.accountNo = '98765432109876';
  gift.accountHolder = '전체항목테스트고객';
  gift.holderSsn1 = '900101';
  gift.holderSsn2 = '1234567';
  gift.isSameAsBank = 'N';
  gift.remark = '사은품 테스트 비고';

  const head = form.value.headGift;
  head.giftName = '본사사은품B';
  head.giftAmount = 50000;
  head.addGiftAmount = 0;
  head.paySchedDate = '2025-03-01';
  head.payDoneDate = '2025-03-10';
  head.paySource = '';
  head.bankName = '신한은행';
  head.accountNo = '1111222233334444';
  head.accountHolder = '전체항목테스트고객';
  head.holderSsn1 = '900101';
  head.holderSsn2 = '1234567';
  head.isSameAsBank = 'N';
  head.remark = '본사 사은품 비고';

  productType.value = 'S0';
  productRows.value = [{
    company: 'SKT',
    region: '서울',
    subscriptionNo: 'SUB20250115001',
    product: '5G 프리미엄',
    productOption: '100GB',
    contract: '24',
    openStatus: 'RECEIPT',
    openDate: '2025-02-01',
    cancelDate: '',
    setTop: '일반',
    vas: '넷플릭스',
    lineCount: 1
  }];

  const mnp = form.value.mnp;
  mnp.isSameAsCust = 'Y';
  mnp.mnpTelNo = '010-1234-5678';
  mnp.ownerName = '전체항목테스트고객';
  mnp.ownerSsn1 = '900101';
  mnp.ownerSsn2 = '1234567';
  mnp.contactNo = '010-1234-5678';
  mnp.issueDate = '2025-01-25';
  mnp.prevCarrier = 'KT';
  mnp.mnpMemo = '번호이동 테스트 메모';
  mnp.remark = '번호이동 비고';

  alert('테스트 데이터가 채워졌습니다. 저장 버튼을 눌러 등록하세요.');
}

async function onSaveCustomer() {
  if (!form.value.customer.custName?.trim()) {
    alert('고객명을 입력해주세요.');
    return;
  }
  if (modalMode.value === 'detail') {
    if (!form.value.customer.custId) {
      alert('고객 정보가 올바르지 않습니다. 상세를 다시 열어주세요.');
      return;
    }
  }
  const idError = validateCustomerIdByType();
  if (idError) {
    alert(idError);
    return;
  }
  const formatError = validateFormats();
  if (formatError) {
    alert(formatError);
    return;
  }
  try {
    const payload = buildRegisterPayload();
    if (modalMode.value === 'detail') {
      await axios.put('/api/customers/update', payload);
      alert('수정되었습니다.');
    } else {
      await axios.post('/api/customers/register', payload);
      alert('등록되었습니다.');
    }
    showCustomerModal.value = false;
    onSearch();
  } catch (err) {
    console.error(modalMode.value === 'detail' ? '고객 수정 오류:' : '고객 등록 오류:', err);
    alert(err.response?.data?.message || (modalMode.value === 'detail' ? '수정' : '등록') + ' 중 오류가 발생했습니다.');
  }
}

const onCustomerRegister = () => {
  openCustomerModal();
};

const onDelete = async row => {
  if (!row?.custId) return;
  if (!confirm('이 고객을 삭제하시겠습니까? 삭제 시 해당 고객의 상품·사은품·납부·번호이동 정보가 함께 삭제됩니다.')) return;
  try {
    await axios.delete(`/api/customers/${row.custId}`);
    alert('삭제되었습니다.');
    onSearch();
  } catch (err) {
    console.error('고객 삭제 오류:', err);
    alert(err.response?.data?.message || '삭제 중 오류가 발생했습니다.');
  }
};

onMounted(async () => {
  await loadCodes();
  onSearch();
});
</script>

<style scoped>
/* 위 필터 줄이고, 가운데 리스트(AG Grid)만 스크롤 되도록 레이아웃 */
.customer-page {
  background: #f4f7f6;
  font-family: 'Noto Sans KR', sans-serif;
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  overflow: hidden;
  padding: 16px 20px;
  box-sizing: border-box;
}

.content-header {
  flex-shrink: 0;
  margin-bottom: 12px;
}

.content-header h2 {
  font-weight: 700;
  color: #333;
  font-size: 1.35rem;
}

.card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.search-section.card {
  padding: 12px 16px;
  margin-bottom: 12px;
  flex-shrink: 0;
}

.search-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  align-items: end;
}

.status-period-wrap {
  grid-column: 1 / -1;
  display: flex;
  align-items: flex-end;
  gap: 8px;
  min-width: 0;
}
.status-period-wrap .input-box.narrow { flex-shrink: 0; }
.status-period-wrap .input-box.period-group { flex: 1; min-width: 0; }

.input-box label {
  display: block;
  font-size: 12px;
  color: #555;
  margin-bottom: 4px;
  font-weight: 600;
}

.input-box input,
.input-box select {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 13px;
}

.input-box input:focus,
.input-box select:focus {
  border-color: #3d5afe;
  outline: none;
}

.input-box.narrow { max-width: 110px; }
.input-box.period-group { min-width: 0; }
.input-box.period-group .period-inline {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: nowrap;
  min-width: 0;
}
.input-box.period-group .period-type-select {
  width: 90px;
  flex-shrink: 0;
}
.input-box.period-group .period-sep { color: #888; font-size: 13px; flex-shrink: 0; }
.input-box.period-group .period-inline input[type="date"] {
  flex: 1 1 120px;
  min-width: 100px;
  max-width: 160px;
}

.input-box.range .date-range {
  display: flex;
  align-items: center;
  gap: 8px;
}

.input-box.range .date-range input {
  flex: 1;
}

.input-box.range .date-range span {
  color: #888;
  font-size: 13px;
}

.search-footer {
  margin-top: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px;
}

.search-footer .search-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
}

.search-footer .right-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-size-select {
  padding: 8px 12px;
  border: 1px solid #dadce0;
  border-radius: 8px;
  font-size: 13px;
  background: #fff;
  min-width: 110px;
}

button {
  padding: 10px 18px;
  border-radius: 8px;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
}

.btn-reset {
  background: #f1f3f4;
  color: #5f6368;
  border: 1px solid #dadce0;
}

.btn-reset:hover {
  background: #e8eaed;
}

.btn-search {
  background: #3d5afe;
  color: white;
}

.btn-search:hover {
  opacity: 0.92;
}

.btn-excel {
  background: #fff;
  color: #5f6368;
  border: 1px solid #dadce0;
}

.btn-excel:hover {
  background: #f8f9fa;
}

.btn-add {
  background: #5a9b5e;
  color: white;
}

.btn-add:hover {
  background: #4d8b51;
}

/* 가운데 리스트(AG Grid)만 스크롤: 그리드 영역이 남는 높이를 채우고 내부만 스크롤 */
.grid-section.card {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  padding: 12px 16px;
  margin-bottom: 0;
  overflow: hidden;
}

/* AG Grid가 높이를 받아 리스트가 보이도록 최소 높이 보장 */
.grid-fill {
  width: 100%;
  flex: 1;
  min-height: 400px;
}

.pagination-info {
  flex-shrink: 0;
  margin-top: 8px;
  font-size: 13px;
  color: #555;
}

.pagination-info strong {
  color: #333;
}

/* 그리드 내 수정/삭제 버튼 (인사관리와 동일 스타일) */
:deep(.btn-table-edit) {
  background: #e8eaf6;
  color: #3949ab;
  padding: 5px 10px;
  font-size: 12px;
  font-weight: 600;
  border-radius: 4px;
  border: none;
  cursor: pointer;
  margin-right: 6px;
}

:deep(.btn-table-edit:hover) {
  background: #c5cae9;
  color: #283593;
}

:deep(.btn-table-del) {
  background: #ffebee;
  color: #c62828;
  padding: 5px 10px;
  font-size: 12px;
  font-weight: 600;
  border-radius: 4px;
  border: none;
  cursor: pointer;
}

:deep(.btn-table-del:hover) {
  background: #ffcdd2;
  color: #b71c1c;
}

:deep(.ag-theme-alpine) {
  --ag-header-background-color: #f8f9fa;
  --ag-row-hover-color: #e3f2fd;
}

:deep(.status-done) {
  color: #2e7d32;
  font-weight: 600;
}

:deep(.status-cancel) {
  color: #c62828;
  font-weight: 600;
}

/* 고객 등록 큰 모달 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
  box-sizing: border-box;
}

.modal-excel {
  background: #fff;
  border-radius: 12px;
  width: 100%;
  max-width: 440px;
  min-width: 320px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}
.modal-excel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
}
.modal-excel-header h3 { margin: 0; font-size: 1.1rem; font-weight: 700; color: #333; }
.modal-excel-body { padding: 20px; display: flex; flex-direction: column; gap: 18px; }
.modal-excel-body label { display: block; margin-bottom: 0; font-size: 13px; color: #555; font-weight: 600; }
.excel-filter-item { display: flex; flex-direction: column; gap: 8px; }
.excel-filter-item label { display: block; margin-bottom: 0; }
.excel-filter-item .period-inline { display: flex; align-items: center; gap: 8px; flex-wrap: nowrap; min-width: 0; }
.excel-filter-item .excel-period-type-select { width: 90px; flex-shrink: 0; padding: 8px 10px; border: 1px solid #ddd; border-radius: 6px; font-size: 13px; }
.excel-filter-item .period-sep { color: #888; flex-shrink: 0; }
.excel-filter-item input[type="date"] { padding: 8px 10px; border: 1px solid #ddd; border-radius: 6px; font-size: 13px; width: 130px; min-width: 0; flex-shrink: 0; }
.excel-filter-item .excel-row-select { width: 100%; max-width: 200px; padding: 10px 12px; border: 1px solid #ddd; border-radius: 8px; font-size: 14px; }
.modal-excel-footer { display: flex; justify-content: flex-end; gap: 10px; padding: 16px 20px; border-top: 1px solid #eee; }
.btn-download { padding: 8px 16px; background: #1976d2; color: #fff; border: none; border-radius: 8px; cursor: pointer; font-weight: 600; }
.btn-download:hover:not(:disabled) { background: #1565c0; }
.btn-download:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-cancel { padding: 8px 16px; background: #f5f5f5; color: #333; border: none; border-radius: 8px; cursor: pointer; }

.modal-customer-reg {
  background: #fff;
  border-radius: 12px;
  width: 90vw;
  max-width: 90vw;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.modal-reg-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #eee;
  flex-shrink: 0;
}

.modal-reg-header h3 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 700;
  color: #333;
}

.modal-close {
  width: 36px;
  height: 36px;
  padding: 0;
  font-size: 1.5rem;
  line-height: 1;
  color: #666;
  background: none;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

.modal-close:hover {
  background: #f0f0f0;
  color: #333;
}

.modal-reg-body {
  padding: 24px;
  overflow-y: auto;
  flex: 1;
  min-height: 0;
}

.reg-section {
  margin-bottom: 24px;
}

.reg-section-title {
  margin: 0 0 16px 0;
  font-size: 1rem;
  font-weight: 700;
  color: #333;
  padding-bottom: 8px;
  border-bottom: 2px solid #3d5afe;
}

/* 등록 모달 테이블 형식 */
.reg-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}
.reg-table th,
.reg-table td {
  border: 1px solid #e0e0e0;
  padding: 10px 12px;
  vertical-align: middle;
}
.reg-table th {
  background: #f5f5f5;
  font-weight: 600;
  color: #555;
  width: 120px;
  white-space: nowrap;
}
.reg-table td {
  background: #fff;
}
.reg-table input,
.reg-table select,
.reg-table textarea {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  box-sizing: border-box;
}
.reg-table textarea.remark-textarea {
  min-height: 100px;
  resize: vertical;
}
.reg-table.product-table thead th,
.reg-table.product-table thead td {
  background: #e8eaf6;
  font-weight: 600;
}
.reg-table .input-with-select,
.reg-table .input-with-check {
  display: flex;
  align-items: center;
  gap: 8px;
}
.reg-table .input-with-select .carrier-select,
.reg-table .auth-fields .auth-type-select { width: 100px; flex-shrink: 0; }
.reg-table .input-with-select input { flex: 1; min-width: 0; }
.reg-table .input-line { width: 60px; }
.reg-table .readonly-cell { background: #f5f5f5; color: #555; }
.reg-table .amount-with-unit {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.reg-table .amount-with-unit input { width: 140px; }
.reg-table .amount-with-unit .unit { color: #666; font-size: 14px; }

.product-type-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.product-type-row label {
  font-weight: 600;
  color: #555;
  min-width: 60px;
}

.product-type-row select {
  padding: 10px 14px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  min-width: 120px;
}

.product-rows {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.product-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.product-row-label {
  font-size: 13px;
  font-weight: 600;
  color: #555;
  min-width: 56px;
}

.product-row input,
.product-row select.product-region-select {
  padding: 8px 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 13px;
  min-width: 0;
  flex: 1;
  max-width: 120px;
}
.product-row select.product-region-select,
.product-row select.product-company-select {
  min-width: 90px;
}

.product-row .input-line {
  max-width: 70px;
}

.product-row .unit {
  font-size: 13px;
  color: #666;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px 20px;
}

.form-item label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #555;
  margin-bottom: 6px;
}

.form-item input,
.form-item select,
.form-item textarea {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
}

.form-item textarea {
  resize: vertical;
  min-height: 60px;
}

.form-item-full { grid-column: 1 / -1; }
.form-item-wide { grid-column: span 2; }

/* 우편번호·주소·상세주소 한 줄 */
.address-row {
  grid-column: 1 / -1;
  display: flex;
  gap: 14px 20px;
  align-items: flex-end;
  flex-wrap: nowrap;
}
.address-row .form-item-zip { flex: 0 0 100px; min-width: 90px; }
.address-row .form-item-addr { flex: 1; min-width: 0; }
.address-row .form-item-addr-detail { flex: 1; min-width: 0; }

.required { color: #d32f2f; }

.radio-group {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 20px;
}

.radio-group label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 0;
  cursor: pointer;
  font-weight: 500;
  white-space: nowrap;
  flex-shrink: 0;
}

.input-split {
  display: flex;
  align-items: center;
  gap: 6px;
}

.input-split input { flex: 1; min-width: 0; }
.input-split span { color: #888; }

.input-split-with-btn {
  display: flex;
  align-items: center;
  gap: 8px;
}
.input-split-with-btn .input-split { flex: 1; }

.input-with-btn {
  display: flex;
  gap: 8px;
}
.input-with-btn input { flex: 1; }

.btn-inline {
  padding: 8px 12px;
  font-size: 13px;
  font-weight: 600;
  border-radius: 6px;
  border: 1px solid #dadce0;
  background: #f8f9fa;
  color: #5f6368;
  white-space: nowrap;
  cursor: pointer;
}
.btn-inline:hover { background: #e8eaed; color: #333; }
.btn-search-inline { background: #3d5afe; color: #fff; border-color: #3d5afe; }
.btn-search-inline:hover { background: #334dd4; }

.input-with-check {
  display: flex;
  align-items: center;
  gap: 10px;
}
.input-with-check input { flex: 1; min-width: 0; }
.check-label.inline { margin-bottom: 0; }

/* 고객명 입력란 넓게 */
.form-item-cust-name { grid-column: span 2; }

/* 고객인증 셀렉트·인풋 너비 제한 */
.form-item-auth { max-width: 220px; }
.auth-fields .auth-type-select { min-width: 0; width: 90px; max-width: 100px; flex-shrink: 0; }
.auth-fields input { min-width: 0; width: 90px; max-width: 110px; }
/* 등록 모달 테이블 안에서는 인증값 입력란 넓게 */
.reg-table .auth-fields input { flex: 1; min-width: 120px; width: auto; max-width: none; }

.remark-textarea {
  min-height: 140px;
  width: 100%;
  box-sizing: border-box;
}

.product-type-only {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
.product-type-only label {
  font-weight: 600;
  color: #555;
  min-width: 40px;
}
.product-type-only select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  min-width: 100px;
}

.input-with-select {
  display: flex;
  gap: 8px;
}

.carrier-select { width: 100px; flex-shrink: 0; }
.input-with-select input { flex: 1; }

.check-label {
  display: flex !important;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-weight: 500;
}

.check-label input { width: auto; }
.check-label.same-as-check { margin-left: 10px; white-space: nowrap; }

.modal-reg-footer {
  padding: 16px 24px;
  border-top: 1px solid #eee;
  background: #f8f9fa;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  flex-shrink: 0;
}

.modal-reg-footer .btn-test-data {
  padding: 8px 14px;
  background: #e8eaf6;
  color: #3949ab;
  border: 1px solid #c5cae9;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
}
.modal-reg-footer .btn-cancel {
  background: #fff;
  color: #5f6368;
  border: 1px solid #dadce0;
}

.modal-reg-footer .btn-save {
  background: #3d5afe;
  color: white;
}
</style>
