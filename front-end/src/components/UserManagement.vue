<template>
  <div class="hr-page">
    <div class="content-header">
      <h2>인사 관리 <small>직원 및 부서 권한 설정</small></h2>
    </div>

    <section class="search-section card">
      <div class="search-grid">
        <div class="input-box">
          <label>아이디</label>
          <input v-model="searchQuery.loginId" @keyup.enter="onSearch" placeholder="아이디 입력" />
        </div>
        <div class="input-box">
          <label>이름</label>
          <input v-model="searchQuery.userName" @keyup.enter="onSearch" placeholder="이름 입력" />
        </div>
        <div class="input-box">
          <label>부서명</label>
          <input v-model="searchQuery.deptName" @keyup.enter="onSearch" placeholder="부서명 입력" />
        </div>
        <div class="input-box">
          <label>권한</label>
          <select v-model="searchQuery.userRole">
            <option value="">전체</option>
            <option value="ADMIN">ADMIN</option>
            <option value="MANAGER">MANAGER</option>
            <option value="MEMBER">MEMBER</option>
          </select>
        </div>
      </div>
      <div class="search-footer">
        <button class="btn-reset" @click="resetSearch">초기화</button>
        <button class="btn-search" @click="onSearch">조회</button>
        <button class="btn-add" @click="openUserModal('add')">직원 추가</button>
      </div>
    </section>

    <section class="grid-section card">
      <ag-grid-vue
          style="width: 100%; height: 400px;"
          class="ag-theme-alpine"
          :columnDefs="columnDefs"
          :rowData="rowData"
          :defaultColDef="defaultColDef"
          :pagination="true"
          :paginationPageSize="10"
          :paginationPageSizeSelector="[10, 20, 50]"
          @grid-ready="onGridReady"
      />
    </section>

    <div v-if="showUserModal" class="modal-overlay">
      <div class="modal-box">
        <div class="modal-header">{{ modalTitle }}</div>
        <div class="modal-body">
          <div class="form-item">
            <label>아이디 <span class="required">*</span></label>
            <div class="input-group">
              <input v-model="userForm.loginId" :disabled="modalMode === 'edit'" placeholder="아이디 입력" />
              <button v-if="modalMode === 'add'" @click="checkIdDuplicate" class="btn-small">중복확인</button>
            </div>
            <p v-if="idChecked" :class="isIdAvailable ? 'msg-ok' : 'msg-no'">{{ idCheckMsg }}</p>
          </div>
          <div class="form-item">
            <label>비밀번호 <span v-if="modalMode === 'add'" class="required">*</span></label>
            <input type="password" v-model="userForm.password" :placeholder="modalMode === 'edit' ? '변경 시에만 입력' : '비밀번호 입력'" />
          </div>
          <div class="form-item">
            <label>이름 <span class="required">*</span></label>
            <input v-model="userForm.userName" placeholder="이름 입력" />
          </div>
          <div class="form-item">
            <label>부서</label>
            <div class="input-group">
              <input v-model="userForm.deptName" readonly placeholder="부서 검색을 이용하세요" class="bg-readonly" />
              <button @click="openDeptSearch" class="btn-small">부서검색</button>
            </div>
          </div>
          <div class="form-item">
            <label>권한</label>
            <select v-model="userForm.userRole">
              <option value="MEMBER">MEMBER</option>
              <option value="MANAGER">MANAGER</option>
              <option value="ADMIN">ADMIN</option>
            </select>
          </div>
          <div class="form-item row-flex">
            <div class="check-item">
              <input type="checkbox" id="isLeader" v-model="userForm.isLeader" :true-value="1" :false-value="0">
              <label for="isLeader">부서 리더</label>
            </div>
            <div class="check-item">
              <input type="checkbox" id="useYn" v-model="userForm.useYn" true-value="Y" false-value="N">
              <label for="useYn">사용 여부</label>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="showUserModal = false">취소</button>
          <button class="btn-save" @click="saveUser">저장</button>
        </div>
      </div>
    </div>

    <div v-if="showDeptModal" class="modal-overlay secondary">
      <div class="modal-box mini">
        <div class="modal-header">부서 선택</div>
        <div class="modal-body">
          <input v-model="deptSearchText" @input="fetchDepts" placeholder="부서명 입력..." class="w100 search-input" />
          <ul class="dept-list">
            <li v-for="dept in deptList" :key="dept.deptId" @click="selectDept(dept)">
              {{ dept.deptName }}
            </li>
            <li v-if="deptList.length === 0" class="no-data">검색 결과가 없습니다.</li>
          </ul>
        </div>
        <div class="modal-footer">
          <button @click="showDeptModal = false">닫기</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';
import { AgGridVue } from "ag-grid-vue3";
import "ag-grid-community/styles/ag-grid.css";
import "ag-grid-community/styles/ag-theme-alpine.css";


// --- ag-Grid 관련 설정 ---
const gridApi = ref(null);
const rowData = ref([]);
const columnDefs = ref([
  { field: "loginId", headerName: "아이디", sortable: true, filter: true },
  { field: "userName", headerName: "이름", sortable: true, filter: true },
  { field: "deptName", headerName: "부서", sortable: true, filter: true },
  {
    field: "userRole",
    headerName: "권한",
    cellClass: params => 'tag-' + (params.value || '').toLowerCase()
  },
  {
    field: "isLeader",
    headerName: "리더",
    valueFormatter: params => params.value === 1 ? 'YES' : '-'
  },
  {
    field: "useYn",
    headerName: "상태",
    valueFormatter: params => params.value === 'Y' ? '사용중' : '중지'
  },
  {
    headerName: "관리",
    cellRenderer: (params) => {
      const container = document.createElement('div');
      container.style.display = 'flex';
      container.style.gap = '5px';
      container.style.justifyContent = 'center';

      const editBtn = document.createElement('button');
      editBtn.innerText = '수정';
      editBtn.className = 'btn-table-edit';
      editBtn.onclick = () => openUserModal('edit', params.data);

      const delBtn = document.createElement('button');
      delBtn.innerText = '삭제';
      delBtn.className = 'btn-table-del';
      delBtn.onclick = () => deleteUser(params.data.userId);

      container.appendChild(editBtn);
      container.appendChild(delBtn);
      return container;
    }
  }
]);
const defaultColDef = { flex: 1, resizable: true };

const onGridReady = (params) => {
  gridApi.value = params.api;
  params.api.sizeColumnsToFit();
};

// --- 상태 관리 변수 ---
const searchQuery = ref({ loginId: '', userName: '', deptName: '', userRole: '', useYn: '' });
const userForm = ref({});
const showUserModal = ref(false);
const showDeptModal = ref(false);
const modalMode = ref('add');
const idChecked = ref(false);
const isIdAvailable = ref(false);
const idCheckMsg = ref('');
const deptList = ref([]);
const deptSearchText = ref('');

const modalTitle = computed(() => modalMode.value === 'add' ? '신규 직원 등록' : '직원 정보 수정');

// --- 데이터 통신 함수 (모두 POST 사용) ---

// 직원 목록 조회
const onSearch = async () => {
  try {
    const res = await axios.post('/api/admin/users/list', searchQuery.value);
    rowData.value = res.data;
  } catch (err) {
    console.error("조회 오류:", err);
  }
};

const resetSearch = () => {
  searchQuery.value = { loginId: '', userName: '', deptName: '', userRole: '', useYn: '' };
  onSearch();
};

// 아이디 중복 체크
const checkIdDuplicate = async () => {
  if(!userForm.value.loginId) {
    alert("아이디를 입력해주세요.");
    return;
  }
  try {
    const res = await axios.post('/api/admin/users/check-id', { loginId: userForm.value.loginId });
    isIdAvailable.value = !res.data.isDuplicate;
    idCheckMsg.value = isIdAvailable.value ? "사용 가능한 아이디입니다." : "이미 존재하는 아이디입니다.";
    idChecked.value = true;
  } catch (err) {
    alert("중복 체크 중 오류가 발생했습니다.");
  }
};

// 부서 검색
const fetchDepts = async () => {
  try {
    const res = await axios.post('/api/admin/users/dept/list', { deptName: deptSearchText.value });
    deptList.value = res.data;
  } catch (err) {
    console.error("부서 조회 오류:", err);
  }
};

const openDeptSearch = () => {
  deptSearchText.value = '';
  fetchDepts();
  showDeptModal.value = true;
};

const selectDept = (dept) => {
  userForm.value.deptId = dept.deptId;
  userForm.value.deptName = dept.deptName;
  showDeptModal.value = false;
};

const resetForm = () => {
  userForm.value = {
    loginId: '',
    userName: '',
    password: '',
    deptId: null,
    deptName: '',
    userRole: 'MEMBER',
    useYn: 'Y',
    isLeader: 0
  };
  idChecked.value = false;
};

// 3. openUserModal 수정
const openUserModal = (mode, data = null) => {
  modalMode.value = mode;
  if(mode === 'add') {
    resetForm();
  } else {
    userForm.value = { ...data, password: '' };
    idChecked.value = true; // 수정 시에는 중복체크 생략 가능하게 설정
  }
  showUserModal.value = true;
};

// 직원 저장 (등록/수정)
const saveUser = async () => {
  if (modalMode.value === 'add' && !idChecked.value) {
    alert("아이디 중복확인을 해주세요.");
    return;
  }
  if (!userForm.value.userName) {
    alert("이름을 입력해주세요.");
    return;
  }

  const url = modalMode.value === 'add' ? '/api/admin/users/register' : '/api/admin/users/modify';
  try {
    await axios.post(url, userForm.value);
    alert("저장되었습니다.");
    showUserModal.value = false;
    onSearch();
  } catch (err) {
    alert("저장 중 오류가 발생했습니다.");
  }
};

// 직원 삭제
const deleteUser = async (userId) => {
  if(!confirm("정말 이 직원을 삭제하시겠습니까?")) return;
  try {
    await axios.post('/api/admin/users/remove', { userId });
    alert("삭제되었습니다.");
    onSearch();
  } catch (err) {
    alert("삭제 중 오류가 발생했습니다.");
  }
};

onMounted(() => {
  onSearch();
});
</script>

<style scoped>
/* 페이지 기본 레이아웃 */
.hr-page {background: #f4f7f6; font-family: 'Noto Sans KR', sans-serif; }
.content-header h2 { margin-bottom: 20px; font-weight: 700; color: #333; }
.content-header small { font-size: 0.5em; color: #888; margin-left: 10px; font-weight: 400; }

/* 카드 스타일 */
.card { background: white; padding: 25px; border-radius: 12px; margin-bottom: 25px; box-shadow: 0 4px 20px rgba(0,0,0,0.08); }

/* 검색 바 스타일 */
.search-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
.input-box label { display: block; font-size: 13px; color: #555; margin-bottom: 8px; font-weight: 600; }
.input-box input, .input-box select {
  width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; transition: border-color 0.3s;
}
.input-box input:focus { border-color: #3d5afe; outline: none; }
.search-footer { text-align: right; margin-top: 20px; display: flex; justify-content: flex-end; gap: 10px; }

/* 버튼 공통 스타일 */
button { padding: 10px; border-radius: 6px; font-weight: 600; cursor: pointer; transition: all 0.2s; border: none; }
.btn-search { background: #3d5afe; color: white; }
.btn-add { background: #2e7d32; color: white; }
.btn-reset { background: #eee; color: #666; }
.btn-primary { background: #3d5afe; color: white; }
.btn-cancel { background: #f5f5f5; color: #333; }
.btn-small { padding: 5px 10px; font-size: 12px; background: #666; color: white; }

/* 모달(팝업) 스타일 */
.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0,0,0,0.6); display: flex; justify-content: center; align-items: center; z-index: 1000;
}
.modal-box { background: white; border-radius: 15px; width: 500px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.3); }
.modal-box.mini { width: 350px; }
.modal-header { background: #3d5afe; color: white; padding: 20px; font-size: 18px; font-weight: 700; }
.modal-body { padding: 25px; }
.modal-footer { padding: 15px 25px; background: #f9f9f9; display: flex; justify-content: flex-end; gap: 10px; }

/* 폼 아이템 스타일 */
.form-item { margin-bottom: 20px; }
.form-item label { display: block; font-weight: 600; margin-bottom: 8px; font-size: 14px; }
.form-item input, .form-item select { width: 100%; padding: 12px; border: 1px solid #ddd; border-radius: 6px; }
.input-group { display: flex; gap: 10px; }
.input-group input { flex: 1; }
.required { color: #e53935; }
.bg-readonly { background: #f9f9f9; cursor: default; }
.form-hint { font-size: 12px; color: #888; margin-top: 5px; }

/* 리더/사용여부 체크박스 라인 */
.row-flex { display: flex; gap: 20px; margin-top: 10px; }
.check-item { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.check-item label { margin-bottom: 0; cursor: pointer; }

/* 부서 목록 팝업 전용 */
.search-input { margin-bottom: 15px; }
.dept-list { list-style: none; padding: 0; max-height: 250px; overflow-y: auto; border: 1px solid #eee; border-radius: 6px; }
.dept-list li { padding: 12px 15px; border-bottom: 1px solid #eee; cursor: pointer; transition: background 0.2s; }
.dept-list li:hover { background: #e3f2fd; color: #3d5afe; font-weight: 600; }
.no-data { text-align: center; color: #999; padding: 20px !important; }

/* 유효성 메시지 */
.msg-ok { color: #2e7d32; font-size: 12px; margin-top: 5px; }
.msg-no { color: #d32f2f; font-size: 12px; margin-top: 5px; }

/* ag-Grid 커스텀 스타일 */
:deep(.ag-theme-alpine) {
  --ag-header-background-color: #f8f9fa;
  --ag-row-hover-color: #e3f2fd;
  --ag-selected-row-background-color: #bbdefb;
}
:deep(.btn-table-edit) { background: #3d5afe; color: white; padding: 5px; font-size: 12px; border-radius: 4px; }
:deep(.btn-table-del) { background: #e53935; color: white; padding: 5px; font-size: 12px; border-radius: 4px; }
</style>