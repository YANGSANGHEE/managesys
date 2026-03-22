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
            <option v-for="opt in roleOptions" :key="opt.codeValue" :value="opt.codeValue">{{ opt.codeName }}</option>
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
              <option v-for="opt in roleOptions" :key="opt.codeValue" :value="opt.codeValue">{{ opt.codeName }}</option>
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
          <button class="btn-cancel" @click="showDeptModal = false">닫기</button>
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
      container.style.alignItems = 'center';
      container.style.height = '100%';

      const editBtn = document.createElement('button');
      editBtn.innerText = '수정';
      editBtn.className = 'btn-table-edit';
      editBtn.onclick = () => openUserModal('edit', params.data);

      const resetBtn = document.createElement('button');
      resetBtn.innerText = '초기화';
      resetBtn.className = 'btn-table-reset';
      resetBtn.onclick = () => resetPassword(params.data.userId, params.data.loginId);

      const delBtn = document.createElement('button');
      delBtn.innerText = '삭제';
      delBtn.className = 'btn-table-del';
      delBtn.onclick = () => deleteUser(params.data.userId);

      container.appendChild(editBtn);
      container.appendChild(resetBtn);
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

// --- 권한 옵션 (공통코드) ---
const roleOptions = ref([]);

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

// 비밀번호 초기화 ({아이디}1234! 로, 서버에서 BCrypt 암호화)
const resetPassword = async (userId, loginId) => {
  if (!userId) return;
  const initPw = loginId ? `${loginId}1234!` : '아이디1234!';
  if (!confirm(`해당 직원의 비밀번호를 "${initPw}" 으로 초기화하시겠습니까?\n로그인 후 반드시 새 비밀번호로 변경해야 합니다.`)) return;
  try {
    await axios.post('/api/admin/users/reset-password', { userId });
    alert(`비밀번호가 "${initPw}" 으로 초기화되었습니다.\n해당 직원은 다음 로그인 시 비밀번호 변경이 필요합니다.`);
    onSearch();
  } catch (err) {
    alert(err.response?.data?.message || "초기화 중 오류가 발생했습니다.");
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

onMounted(async () => {
  try {
    const res = await axios.post('/api/codes/list', { groupCode: 'USER_ROLE' });
    roleOptions.value = res.data;
  } catch (e) {
    roleOptions.value = [
      { codeValue: 'MEMBER', codeName: 'MEMBER' },
      { codeValue: 'MANAGER', codeName: 'MANAGER' },
      { codeValue: 'ADMIN', codeName: 'ADMIN' },
    ];
  }
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
.input-box input:focus { border-color: #2563eb; outline: none; }
.search-footer { text-align: right; margin-top: 20px; display: flex; justify-content: flex-end; gap: 10px; }

/* 버튼 공통 베이스 */
button {
  padding: 10px 18px;
  border-radius: 8px;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
  min-height: 40px;
  line-height: 1.2;
}
button:hover { opacity: 0.92; }
button:active { transform: scale(0.98); }

/* 검색 영역 버튼 */
.search-footer button { min-width: 90px; }
.btn-reset {
  background: #f1f3f4;
  color: #5f6368;
  border: 1px solid #dadce0;
}
.btn-reset:hover { background: #e8eaed; color: #202124; }
.btn-search {
  background: #2563eb;
  color: white;
  box-shadow: 0 1px 3px rgba(61, 90, 254, 0.3);
}
.btn-search:hover { box-shadow: 0 2px 6px rgba(61, 90, 254, 0.4); }
.btn-add {
  background: #5a9b5e;
  color: white;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
}
.btn-add:hover { background: #4d8b51; box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08); }

/* 모달 내 보조 버튼 (중복확인, 부서검색) */
.btn-small {
  padding: 8px 14px;
  font-size: 13px;
  font-weight: 600;
  min-height: auto;
  background: #5f6368;
  color: white;
  border-radius: 6px;
  white-space: nowrap;
}
.btn-small:hover { background: #4a4d52; }

/* 모달 푸터 버튼 */
.btn-cancel {
  background: #fff;
  color: #5f6368;
  border: 1px solid #dadce0;
}
.btn-cancel:hover { background: #f8f9fa; color: #202124; }
.btn-save {
  background: #2563eb;
  color: white;
  padding: 10px 24px;
  box-shadow: 0 1px 3px rgba(61, 90, 254, 0.3);
}
.btn-save:hover { box-shadow: 0 2px 6px rgba(61, 90, 254, 0.4); }

/* 모달(팝업) 스타일 */
.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0,0,0,0.6); display: flex; justify-content: center; align-items: center; z-index: 1000;
}
.modal-box { background: white; border-radius: 15px; width: 500px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.3); }
.modal-box.mini { width: 350px; }
.modal-header { background: #2563eb; color: white; padding: 20px; font-size: 18px; font-weight: 700; }
.modal-body { padding: 25px; }
.modal-footer { padding: 16px 24px; background: #f8f9fa; display: flex; justify-content: flex-end; gap: 12px; border-top: 1px solid #eee; }

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
.dept-list li:hover { background: #eff6ff; color: #2563eb; font-weight: 600; }
.no-data { text-align: center; color: #999; padding: 20px !important; }

/* 유효성 메시지 */
.msg-ok { color: #2e7d32; font-size: 12px; margin-top: 5px; }
.msg-no { color: #d32f2f; font-size: 12px; margin-top: 5px; }

/* ag-Grid 커스텀 스타일 */
:deep(.ag-theme-alpine) {
  --ag-header-background-color: #f8f9fa;
  --ag-row-hover-color: #eff6ff;
  --ag-selected-row-background-color: #bfdbfe;
}
:deep(.btn-table-edit) {
  background: #eff6ff;
  color: #2563eb;
  padding: 4px 10px;
  font-size: 12px;
  line-height: 1;
  display: inline-flex;
  align-items: center;
  font-weight: 600;
  border-radius: 4px;
  border: none;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-right: 6px;
}
:deep(.btn-table-edit:hover) { background: #dbeafe; color: #1e40af; }
:deep(.btn-table-reset) {
  background: #fff3e0;
  color: #e65100;
  padding: 4px 10px;
  font-size: 12px;
  line-height: 1;
  display: inline-flex;
  align-items: center;
  font-weight: 600;
  border-radius: 4px;
  border: none;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-right: 6px;
}
:deep(.btn-table-reset:hover) { background: #ffe0b2; color: #bf360c; }
:deep(.btn-table-del) {
  background: #ffebee;
  color: #c62828;
  padding: 4px 10px;
  font-size: 12px;
  line-height: 1;
  display: inline-flex;
  align-items: center;
  font-weight: 600;
  border-radius: 4px;
  border: none;
  cursor: pointer;
  transition: all 0.2s ease;
}
:deep(.btn-table-del:hover) { background: #ffcdd2; color: #b71c1c; }
</style>