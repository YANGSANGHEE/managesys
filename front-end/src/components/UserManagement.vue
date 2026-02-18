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
        <button class="btn-search" @click="onSearch">조회</button>
        <button class="btn-add" @click="openUserModal('add')">직원 추가</button>
      </div>
    </section>

    <section class="grid-section card">
      <ag-grid-vue
          style="width: 100%; height: 500px;"
          class="ag-theme-alpine"
          :columnDefs="columnDefs"
          :rowData="rowData"
          :defaultColDef="defaultColDef"
          @grid-ready="onGridReady"
      />
    </section>

    <div v-if="showUserModal" class="modal-overlay">
      <div class="modal-box">
        <div class="modal-header">{{ modalTitle }}</div>
        <div class="modal-body">
          <div class="form-item">
            <label>아이디</label>
            <div class="input-group">
              <input v-model="userForm.loginId" :disabled="modalMode==='edit'" />
              <button v-if="modalMode==='add'" @click="checkIdDuplicate" class="btn-small">중복확인</button>
            </div>
            <p v-if="idChecked" :class="isIdAvailable ? 'msg-ok' : 'msg-no'">{{ idCheckMsg }}</p>
          </div>
          <div class="form-item">
            <label>비밀번호</label>
            <input type="password" v-model="userForm.password" :placeholder="modalMode==='edit' ? '변경시에만 입력' : ''" />
          </div>
          <div class="form-item">
            <label>이름</label>
            <input v-model="userForm.userName" />
          </div>
          <div class="form-item">
            <label>부서</label>
            <div class="input-group">
              <input v-model="userForm.deptName" readonly placeholder="부서 검색을 이용하세요" />
              <button @click="showDeptModal = true" class="btn-small">부서검색</button>
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
        </div>
        <div class="modal-footer">
          <button @click="showUserModal = false">취소</button>
          <button class="btn-primary" @click="saveUser">저장</button>
        </div>
      </div>
    </div>

    <div v-if="showDeptModal" class="modal-overlay secondary">
      <div class="modal-box mini">
        <div class="modal-header">부서 선택</div>
        <div class="modal-body">
          <input v-model="deptSearchText" @input="fetchDepts" placeholder="부서명 입력..." class="w100" />
          <ul class="dept-list">
            <li v-for="dept in deptList" :key="dept.deptId" @click="selectDept(dept)">
              {{ dept.deptName }}
            </li>
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
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { AgGridVue } from "ag-grid-vue3";
import "ag-grid-community/styles/ag-grid.css";
import "ag-grid-community/styles/ag-theme-alpine.css";

// ag-Grid 설정
const rowData = ref([]);
const columnDefs = ref([
  { field: "loginId", headerName: "아이디" },
  { field: "userName", headerName: "이름" },
  { field: "deptName", headerName: "부서" },
  { field: "userRole", headerName: "권한", cellClass: params => 'tag-' + params.value },
  { field: "useYn", headerName: "상태" },
  {
    headerName: "관리",
    cellRenderer: (params) => {
      const btn = document.createElement('button');
      btn.innerText = '수정';
      btn.onclick = () => openUserModal('edit', params.data);
      return btn;
    }
  }
]);
const defaultColDef = { flex: 1, resizable: true };

// 상태 관리
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

// 데이터 페칭
const onSearch = async () => {
  const res = await axios.post('/api/admin/users/list', searchQuery.value);
  rowData.value = res.data;
};

// 아이디 중복 체크
const checkIdDuplicate = async () => {
  if(!userForm.value.loginId) return;
  const res = await axios.post('/api/admin/users/check-id', { loginId: userForm.value.loginId });
  isIdAvailable.value = !res.data.isDuplicate;
  idCheckMsg.value = isIdAvailable.value ? "사용 가능한 아이디입니다." : "이미 존재하는 아이디입니다.";
  idChecked.value = true;
};

// 부서 검색
const fetchDepts = async () => {
  const res = await axios.post('/api/admin/users/dept/list', { deptName: deptSearchText.value });
  deptList.value = res.data;
};

const selectDept = (dept) => {
  userForm.value.deptId = dept.deptId;
  userForm.value.deptName = dept.deptName;
  showDeptModal.value = false;
};

const openUserModal = (mode, data = null) => {
  modalMode.value = mode;
  idChecked.value = false;
  if(mode === 'add') {
    userForm.value = { userRole: 'MEMBER', useYn: 'Y', isLeader: 0 };
  } else {
    userForm.value = { ...data, password: '' };
  }
  showUserModal.value = true;
};

const saveUser = async () => {
  if(modalMode.value === 'add' && !isIdAvailable.value) {
    alert("아이디 중복확인을 해주세요.");
    return;
  }
  const url = modalMode.value === 'add' ? '/api/admin/users/register' : '/api/admin/users/modify';
  await axios.post(url, userForm.value);
  alert("저장되었습니다.");
  showUserModal.value = false;
  onSearch();
};

onMounted(() => {
  onSearch();
  fetchDepts();
});
</script>

<style scoped>
/* ag-Grid 테마 커스텀 및 레이아웃 */
.hr-page { padding: 20px; background: #f0f2f5; }
.card { background: white; padding: 20px; border-radius: 8px; margin-bottom: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
.search-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 15px; }
.input-box label { display: block; font-size: 12px; color: #666; margin-bottom: 5px; }
.input-box input, .input-box select { width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px; }
.search-footer { text-align: right; margin-top: 15px; }

/* 모달 스타일 */
.modal-overlay { position: fixed; top:0; left:0; width:100%; height:100%; background:rgba(0,0,0,0.5); display:flex; justify-content:center; align-items:center; z-index:100; }
.modal-box { background:white; padding:25px; border-radius:12px; width:450px; }
.modal-box.mini { width: 300px; }
.input-group { display: flex; gap: 5px; }
.dept-list { list-style:none; padding:0; margin-top:10px; max-height:200px; overflow-y:auto; }
.dept-list li { padding:10px; border-bottom:1px solid #eee; cursor:pointer; }
.dept-list li:hover { background:#f0f7ff; }
.msg-ok { color: green; font-size: 12px; }
.msg-no { color: red; font-size: 12px; }
</style>