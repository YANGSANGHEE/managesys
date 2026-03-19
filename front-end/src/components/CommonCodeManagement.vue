<template>
  <div class="code-page">
    <div class="content-header">
      <h2>공통코드 관리 <small>그룹코드 및 상세코드 CRUD</small></h2>
    </div>

    <div class="split-layout">
      <!-- ───── 좌측: 그룹코드 ───── -->
      <section class="card panel-left">
        <div class="panel-header">
          <span class="panel-title">그룹코드</span>
          <div class="panel-actions">
            <input v-model="groupSearch" @keyup.enter="fetchGroups" placeholder="그룹명 검색" class="inline-search" />
            <button class="btn-search" @click="fetchGroups">조회</button>
            <button class="btn-add" @click="openGroupModal('add')">추가</button>
          </div>
        </div>
        <ag-grid-vue
          style="width:100%; height:520px;"
          class="ag-theme-alpine"
          :columnDefs="groupCols"
          :rowData="groupData"
          :defaultColDef="defaultColDef"
          :pagination="true"
          :paginationPageSize="15"
          :paginationPageSizeSelector="[15, 30, 50]"
          :rowSelection="{ mode: 'singleRow', checkboxes: false, enableClickSelection: true }"
          @row-clicked="onGroupRowClick"
          @grid-ready="onGroupGridReady"
        />
      </section>

      <!-- ───── 우측: 상세코드 ───── -->
      <section class="card panel-right">
        <div class="panel-header">
          <span class="panel-title">
            상세코드
            <span v-if="selectedGroup" class="selected-badge">{{ selectedGroup.groupCode }} - {{ selectedGroup.groupName }}</span>
          </span>
          <div class="panel-actions">
            <button class="btn-add" :disabled="!selectedGroup" @click="openDetailModal('add')">추가</button>
          </div>
        </div>
        <ag-grid-vue
          style="width:100%; height:520px;"
          class="ag-theme-alpine"
          :columnDefs="detailCols"
          :rowData="detailData"
          :defaultColDef="defaultColDef"
          :pagination="true"
          :paginationPageSize="15"
          :paginationPageSizeSelector="[15, 30, 50]"
          @grid-ready="onDetailGridReady"
        />
        <p v-if="!selectedGroup" class="hint-text">← 좌측에서 그룹코드를 선택하세요</p>
      </section>
    </div>

    <!-- ═══ 그룹코드 모달 ═══ -->
    <div v-if="showGroupModal" class="modal-overlay">
      <div class="modal-box">
        <div class="modal-header">{{ groupModalTitle }}</div>
        <div class="modal-body">
          <div class="form-item">
            <label>그룹코드 <span class="required">*</span></label>
            <input v-model="groupForm.groupCode" :disabled="groupModalMode === 'edit'" placeholder="예) CUST_TYPE" />
          </div>
          <div class="form-item">
            <label>그룹명 <span class="required">*</span></label>
            <input v-model="groupForm.groupName" placeholder="예) 고객 종류" />
          </div>
          <div class="form-item">
            <label>비고</label>
            <input v-model="groupForm.remark" placeholder="설명 (선택)" />
          </div>
          <div class="form-item row-flex">
            <div class="check-item">
              <input type="checkbox" id="groupUseYn" v-model="groupForm.useYn" true-value="Y" false-value="N" />
              <label for="groupUseYn">사용 여부</label>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="showGroupModal = false">취소</button>
          <button class="btn-save" @click="saveGroup">저장</button>
        </div>
      </div>
    </div>

    <!-- ═══ 상세코드 모달 ═══ -->
    <div v-if="showDetailModal" class="modal-overlay">
      <div class="modal-box">
        <div class="modal-header">{{ detailModalTitle }}</div>
        <div class="modal-body">
          <div class="form-item">
            <label>그룹코드</label>
            <input :value="detailForm.groupCode" disabled class="bg-readonly" />
          </div>
          <div class="form-item">
            <label>코드값 <span class="required">*</span></label>
            <input v-model="detailForm.codeValue" :disabled="detailModalMode === 'edit'" placeholder="예) INDIVIDUAL" />
          </div>
          <div class="form-item">
            <label>코드명 <span class="required">*</span></label>
            <input v-model="detailForm.codeName" placeholder="예) 개인고객" />
          </div>
          <div class="form-item">
            <label>정렬순서</label>
            <input v-model.number="detailForm.sortOrder" type="number" min="1" placeholder="1" />
          </div>
          <div class="form-item">
            <label>비고</label>
            <input v-model="detailForm.remark" placeholder="설명 (선택)" />
          </div>
          <div class="form-item row-flex">
            <div class="check-item">
              <input type="checkbox" id="detailUseYn" v-model="detailForm.useYn" true-value="Y" false-value="N" />
              <label for="detailUseYn">사용 여부</label>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="showDetailModal = false">취소</button>
          <button class="btn-save" @click="saveDetail">저장</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import axios from 'axios';
import { AgGridVue } from 'ag-grid-vue3';
import 'ag-grid-community/styles/ag-grid.css';
import 'ag-grid-community/styles/ag-theme-alpine.css';

// ── ag-Grid ──────────────────────────────────────────────────────────────────
const groupGridApi  = ref(null);
const detailGridApi = ref(null);
const defaultColDef = { flex: 1, resizable: true, minWidth: 60 };

const groupCols = ref([
  { field: 'groupCode', headerName: '그룹코드', width: 140, minWidth: 100 },
  { field: 'groupName', headerName: '그룹명',   flex: 1 },
  { field: 'useYn',     headerName: '사용',     width: 70,  cellStyle: params => ({ color: params.value === 'Y' ? '#2e7d32' : '#c62828', fontWeight: 600 }) },
  { field: 'remark',    headerName: '비고',     flex: 1 },
  {
    headerName: '관리',
    width: 130,
    cellRenderer: (params) => {
      const wrap = document.createElement('div');
      wrap.style.cssText = 'display:flex;gap:5px;justify-content:center;align-items:center;height:100%';

      const editBtn = document.createElement('button');
      editBtn.innerText = '수정';
      editBtn.className = 'btn-table-edit';
      editBtn.onclick = (e) => { e.stopPropagation(); openGroupModal('edit', params.data); };

      const delBtn = document.createElement('button');
      delBtn.innerText = '삭제';
      delBtn.className = 'btn-table-del';
      delBtn.onclick = (e) => { e.stopPropagation(); deleteGroup(params.data.groupCode); };

      wrap.appendChild(editBtn);
      wrap.appendChild(delBtn);
      return wrap;
    }
  }
]);

const detailCols = ref([
  { field: 'codeValue', headerName: '코드값', width: 140, minWidth: 100 },
  { field: 'codeName',  headerName: '코드명', flex: 1 },
  { field: 'sortOrder', headerName: '순서',   width: 70 },
  { field: 'useYn',     headerName: '사용',   width: 70, cellStyle: params => ({ color: params.value === 'Y' ? '#2e7d32' : '#c62828', fontWeight: 600 }) },
  { field: 'remark',    headerName: '비고',   flex: 1 },
  {
    headerName: '관리',
    width: 110,
    cellRenderer: (params) => {
      const wrap = document.createElement('div');
      wrap.style.cssText = 'display:flex;gap:5px;justify-content:center;align-items:center;height:100%';

      const editBtn = document.createElement('button');
      editBtn.innerText = '수정';
      editBtn.className = 'btn-table-edit';
      editBtn.onclick = () => openDetailModal('edit', params.data);

      const delBtn = document.createElement('button');
      delBtn.innerText = '삭제';
      delBtn.className = 'btn-table-del';
      delBtn.onclick = () => deleteDetail(params.data.groupCode, params.data.codeValue);

      wrap.appendChild(editBtn);
      wrap.appendChild(delBtn);
      return wrap;
    }
  }
]);

const onGroupGridReady  = (p) => { groupGridApi.value  = p.api; };
const onDetailGridReady = (p) => { detailGridApi.value = p.api; };

// ── 상태 ──────────────────────────────────────────────────────────────────────
const groupData    = ref([]);
const detailData   = ref([]);
const groupSearch  = ref('');
const selectedGroup = ref(null);

// 그룹코드 모달
const showGroupModal  = ref(false);
const groupModalMode  = ref('add');
const groupForm       = ref({});
const groupModalTitle = computed(() => groupModalMode.value === 'add' ? '그룹코드 추가' : '그룹코드 수정');

// 상세코드 모달
const showDetailModal  = ref(false);
const detailModalMode  = ref('add');
const detailForm       = ref({});
const detailModalTitle = computed(() => detailModalMode.value === 'add' ? '상세코드 추가' : '상세코드 수정');

// ── 데이터 조회 ───────────────────────────────────────────────────────────────
const fetchGroups = async () => {
  try {
    const res = await axios.post('/api/codes/groups/list', { groupName: groupSearch.value });
    groupData.value = res.data;
  } catch (err) {
    const status = err.response?.status;
    const msg = status === 404
      ? '서버에 공통코드 관리 API가 없습니다. 백엔드 서버를 재시작해주세요.'
      : `그룹코드 조회 오류 (${status ?? 'network error'})`;
    alert(msg);
    console.error('[공통코드] 그룹 조회 실패:', err);
  }
};

const fetchDetail = async (groupCode) => {
  try {
    const res = await axios.post('/api/codes/detail/list', { groupCode });
    detailData.value = res.data;
  } catch {
    alert('상세코드 조회 중 오류가 발생했습니다.');
  }
};

const onGroupRowClick = (e) => {
  selectedGroup.value = e.data;
  fetchDetail(e.data.groupCode);
};

// ── 그룹코드 CRUD ─────────────────────────────────────────────────────────────
const openGroupModal = (mode, data = null) => {
  groupModalMode.value = mode;
  if (mode === 'add') {
    groupForm.value = { groupCode: '', groupName: '', useYn: 'Y', remark: '' };
  } else {
    groupForm.value = { ...data };
  }
  showGroupModal.value = true;
};

const saveGroup = async () => {
  if (!groupForm.value.groupCode?.trim()) { alert('그룹코드를 입력해주세요.'); return; }
  if (!groupForm.value.groupName?.trim()) { alert('그룹명을 입력해주세요.'); return; }
  const url = groupModalMode.value === 'add' ? '/api/codes/groups/register' : '/api/codes/groups/modify';
  try {
    await axios.post(url, groupForm.value);
    alert('저장되었습니다.');
    showGroupModal.value = false;
    fetchGroups();
  } catch (err) {
    alert(err.response?.data || '저장 중 오류가 발생했습니다.');
  }
};

const deleteGroup = async (groupCode) => {
  if (!confirm(`그룹코드 [${groupCode}]를 삭제하시겠습니까?`)) return;
  try {
    await axios.post('/api/codes/groups/remove', { groupCode });
    alert('삭제되었습니다.');
    if (selectedGroup.value?.groupCode === groupCode) {
      selectedGroup.value = null;
      detailData.value = [];
    }
    fetchGroups();
  } catch (err) {
    alert(err.response?.data || '삭제 중 오류가 발생했습니다.');
  }
};

// ── 상세코드 CRUD ─────────────────────────────────────────────────────────────
const openDetailModal = (mode, data = null) => {
  detailModalMode.value = mode;
  if (mode === 'add') {
    detailForm.value = { groupCode: selectedGroup.value.groupCode, codeValue: '', codeName: '', sortOrder: 1, useYn: 'Y', remark: '' };
  } else {
    detailForm.value = { ...data };
  }
  showDetailModal.value = true;
};

const saveDetail = async () => {
  if (!detailForm.value.codeValue?.trim()) { alert('코드값을 입력해주세요.'); return; }
  if (!detailForm.value.codeName?.trim())  { alert('코드명을 입력해주세요.'); return; }
  const url = detailModalMode.value === 'add' ? '/api/codes/detail/register' : '/api/codes/detail/modify';
  try {
    await axios.post(url, detailForm.value);
    alert('저장되었습니다.');
    showDetailModal.value = false;
    fetchDetail(detailForm.value.groupCode);
  } catch (err) {
    alert(err.response?.data || '저장 중 오류가 발생했습니다.');
  }
};

const deleteDetail = async (groupCode, codeValue) => {
  if (!confirm(`코드값 [${codeValue}]를 삭제하시겠습니까?`)) return;
  try {
    await axios.post('/api/codes/detail/remove', { groupCode, codeValue });
    alert('삭제되었습니다.');
    fetchDetail(groupCode);
  } catch (err) {
    alert(err.response?.data || '삭제 중 오류가 발생했습니다.');
  }
};

onMounted(() => fetchGroups());
</script>

<style scoped>
.code-page { background: #f4f7f6; font-family: 'Noto Sans KR', sans-serif; }
.content-header h2 { margin-bottom: 20px; font-weight: 700; color: #333; }
.content-header small { font-size: 0.5em; color: #888; margin-left: 10px; font-weight: 400; }

/* 2-column split */
.split-layout { display: grid; grid-template-columns: 6fr 4fr; gap: 20px; }
@media (max-width: 1100px) { .split-layout { grid-template-columns: 1fr; } }

.card { background: white; padding: 20px; border-radius: 12px; margin-bottom: 0; box-shadow: 0 4px 20px rgba(0,0,0,0.08); }

/* panel header */
.panel-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; flex-wrap: wrap; gap: 8px; }
.panel-title { font-size: 15px; font-weight: 700; color: #333; }
.selected-badge { margin-left: 8px; font-size: 12px; font-weight: 600; color: #3d5afe; background: #e8eaf6; padding: 2px 8px; border-radius: 10px; }
.panel-actions { display: flex; gap: 8px; align-items: center; }
.inline-search { padding: 8px 10px; border: 1px solid #ddd; border-radius: 6px; font-size: 13px; width: 130px; }

.hint-text { text-align: center; color: #aaa; font-size: 13px; margin-top: 12px; }

/* 버튼 */
button { padding: 8px 16px; border-radius: 8px; font-weight: 600; font-size: 13px; cursor: pointer; transition: all 0.2s ease; border: none; }
button:disabled { opacity: 0.4; cursor: not-allowed; }
.btn-search { background: #3d5afe; color: white; }
.btn-search:hover:not(:disabled) { opacity: 0.88; }
.btn-add { background: #5a9b5e; color: white; }
.btn-add:hover:not(:disabled) { background: #4d8b51; }
.btn-cancel { background: #fff; color: #5f6368; border: 1px solid #dadce0; }
.btn-cancel:hover { background: #f8f9fa; }
.btn-save { background: #3d5afe; color: white; padding: 10px 24px; }
.btn-save:hover { opacity: 0.88; }

/* 모달 */
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.6); display: flex; justify-content: center; align-items: center; z-index: 1000; }
.modal-box { background: white; border-radius: 15px; width: 460px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.3); }
.modal-header { background: #3d5afe; color: white; padding: 20px; font-size: 18px; font-weight: 700; }
.modal-body { padding: 25px; }
.modal-footer { padding: 16px 24px; background: #f8f9fa; display: flex; justify-content: flex-end; gap: 12px; border-top: 1px solid #eee; }

.form-item { margin-bottom: 18px; }
.form-item label { display: block; font-weight: 600; margin-bottom: 7px; font-size: 14px; }
.form-item input { width: 100%; padding: 11px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; box-sizing: border-box; }
.form-item input:focus { border-color: #3d5afe; outline: none; }
.required { color: #e53935; }
.bg-readonly { background: #f5f5f5; cursor: default; }
.row-flex { display: flex; gap: 20px; }
.check-item { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.check-item label { margin-bottom: 0; cursor: pointer; font-weight: 500; }

/* ag-Grid */
:deep(.ag-theme-alpine) {
  --ag-header-background-color: #f8f9fa;
  --ag-row-hover-color: #e3f2fd;
  --ag-selected-row-background-color: #bbdefb;
}
:deep(.btn-table-edit) {
  background: #e8eaf6; color: #3949ab;
  padding: 4px 10px; font-size: 12px; font-weight: 600;
  border-radius: 4px; border: none; cursor: pointer;
}
:deep(.btn-table-edit:hover) { background: #c5cae9; }
:deep(.btn-table-del) {
  background: #ffebee; color: #c62828;
  padding: 4px 10px; font-size: 12px; font-weight: 600;
  border-radius: 4px; border: none; cursor: pointer;
}
:deep(.btn-table-del:hover) { background: #ffcdd2; }
</style>
