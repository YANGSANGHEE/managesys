<template>
  <div class="notice-page">
    <div class="bento-grid">
      <section v-for="section in sections" :key="section.type" class="bento-card">
        <div class="card-header">
          <h3>{{ section.title }}</h3>
          <button v-if="canWrite" class="add-btn" @click="openCreateModal(section.type)" title="공지 등록">+</button>
        </div>
        <table class="data-table">
          <thead>
            <tr>
              <th>제목</th>
              <th>등록자</th>
              <th>등록일</th>
              <th v-if="canWrite" class="col-action"></th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="section.loading">
              <td :colspan="canWrite ? 4 : 3" class="empty-cell">불러오는 중...</td>
            </tr>
            <tr v-else-if="section.items.length === 0">
              <td :colspan="canWrite ? 4 : 3" class="empty-cell">내용이 없습니다.</td>
            </tr>
            <template v-else>
              <tr
                v-for="item in section.items"
                :key="item.noticeId"
                class="data-row"
                @click="openViewModal(item)"
              >
                <td class="title-cell">
                  <span v-if="item.isFixed === 'Y'" class="fixed-badge">고정</span>
                  {{ item.title }}
                </td>
                <td>{{ item.creatorName }}</td>
                <td class="date-cell">{{ item.createdAt }}</td>
                <td v-if="canWrite" class="action-cell" @click.stop>
                  <button
                    v-if="item.creatorId === currentUserId"
                    class="edit-btn"
                    @click="openEditModal(item, section)"
                  >수정</button>
                  <button
                    v-if="isAdmin || item.creatorId === currentUserId"
                    class="del-btn"
                    @click="deleteNotice(item, section)"
                  >삭제</button>
                </td>
              </tr>
            </template>
          </tbody>
        </table>
      </section>
    </div>

    <!-- 등록 모달 -->
    <div v-if="createModal.visible" class="modal-overlay">
      <div class="modal-box">
        <h3 class="modal-title">공지 등록</h3>
        <div class="form-group">
          <label>제목 <span class="required">*</span></label>
          <input
            v-model="createModal.title"
            type="text"
            class="form-input"
            placeholder="제목을 입력하세요"
            maxlength="200"
          />
        </div>
        <div class="form-group">
          <label>내용 <span class="required">*</span></label>
          <div ref="editorContainer" class="quill-editor-wrap"></div>
        </div>
        <div class="form-group checkbox-row">
          <label class="checkbox-label">
            <input type="checkbox" v-model="createModal.isFixed" />
            상단 고정
          </label>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="closeCreateModal">취소</button>
          <button class="btn-submit" @click="submitCreate" :disabled="createModal.submitting">
            {{ createModal.submitting ? '등록 중...' : '등록' }}
          </button>
        </div>
      </div>
    </div>


    <!-- 수정 모달 -->
    <div v-if="editModal.visible" class="modal-overlay">
      <div class="modal-box">
        <h3 class="modal-title">공지 수정</h3>
        <div class="form-group">
          <label>제목 <span class="required">*</span></label>
          <input
            v-model="editModal.title"
            type="text"
            class="form-input"
            placeholder="제목을 입력하세요"
            maxlength="200"
          />
        </div>
        <div class="form-group">
          <label>내용 <span class="required">*</span></label>
          <div ref="editEditorContainer" class="quill-editor-wrap"></div>
        </div>
        <div class="form-group checkbox-row">
          <label class="checkbox-label">
            <input type="checkbox" v-model="editModal.isFixed" />
            상단 고정
          </label>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="closeEditModal">취소</button>
          <button class="btn-submit" @click="submitEdit" :disabled="editModal.submitting">
            {{ editModal.submitting ? '저장 중...' : '저장' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 보기 모달 -->
    <div v-if="viewModal.visible" class="modal-overlay">
      <div class="modal-box modal-view">
        <div class="view-header">
          <div class="view-title-row">
            <span v-if="viewModal.item?.isFixed === 'Y'" class="fixed-badge">고정</span>
            <h3>{{ viewModal.item?.title }}</h3>
          </div>
          <button class="close-btn" @click="closeViewModal">✕</button>
        </div>
        <div class="view-meta">
          <span>등록자: {{ viewModal.item?.creatorName }}</span>
          <span>등록일: {{ viewModal.item?.createdAt }}</span>
        </div>
        <!-- Quill HTML 렌더링: ql-snow ql-editor 클래스로 Quill 스타일 적용 -->
        <div class="ql-snow view-content-wrap">
          <div class="ql-editor view-content" v-html="viewModal.item?.content"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, computed, ref, watch, nextTick, onMounted } from 'vue';
import axios from 'axios';
import { useAuthStore } from '@/store/auth';
import Quill from 'quill';
import 'quill/dist/quill.snow.css';

const authStore = useAuthStore();

const isAdmin = computed(() => authStore.user?.userRole === 'ADMIN');
// 등록/수정/삭제 버튼 컬럼 표시 여부 (ADMIN + MANAGER)
const canWrite = computed(() => ['ADMIN', 'MANAGER'].includes(authStore.user?.userRole));
const currentUserId = computed(() => authStore.user?.userId);

// 사내자료실(COMPANY_ROOM) · 협력점 자료실(PARTNER_ROOM) 숨김
const sections = reactive([
  { type: 'COMPANY', title: '사내 공지사항', items: [], loading: false },
  { type: 'PARTNER', title: '협력점 공지사항', items: [], loading: false },
]);

const createModal = reactive({
  visible: false,
  noticeType: '',
  title: '',
  isFixed: false,
  submitting: false,
});

const viewModal = reactive({
  visible: false,
  item: null,
});

const editorContainer = ref(null);
let quillInstance = null;

const editModal = reactive({
  visible: false,
  noticeId: null,
  noticeType: '',
  title: '',
  isFixed: false,
  submitting: false,
  _initialContent: '',
  _section: null,
});

const editEditorContainer = ref(null);
let editQuillInstance = null;

function imageHandler(quill) {
  const input = document.createElement('input');
  input.setAttribute('type', 'file');
  input.setAttribute('accept', 'image/*');
  input.click();
  input.onchange = () => {
    const file = input.files[0];
    if (!file) return;
    if (file.size > 5 * 1024 * 1024) {
      alert('이미지 크기는 5MB 이하만 첨부 가능합니다.');
      return;
    }
    const reader = new FileReader();
    reader.onload = (e) => {
      const range = quill.getSelection(true);
      quill.insertEmbed(range.index, 'image', e.target.result);
      quill.setSelection(range.index + 1);
    };
    reader.readAsDataURL(file);
  };
}

function makeQuillConfig() {
  return {
    theme: 'snow',
    placeholder: '내용을 입력하세요',
    modules: {
      toolbar: {
        container: [
          ['bold', 'italic', 'underline', 'strike'],
          [{ header: [1, 2, 3, false] }],
          [{ list: 'ordered' }, { list: 'bullet' }],
          [{ color: [] }, { background: [] }],
          ['image'],
        ],
      },
    },
  };
}

// 등록 모달 열릴 때 Quill 초기화
watch(() => createModal.visible, async (visible) => {
  if (visible) {
    await nextTick();
    quillInstance = new Quill(editorContainer.value, makeQuillConfig());
    quillInstance.getModule('toolbar').addHandler('image', () => imageHandler(quillInstance));
  } else {
    quillInstance = null;
  }
});

// 수정 모달 열릴 때 Quill 초기화 및 기존 내용 로드
watch(() => editModal.visible, async (visible) => {
  if (visible) {
    await nextTick();
    editQuillInstance = new Quill(editEditorContainer.value, makeQuillConfig());
    editQuillInstance.getModule('toolbar').addHandler('image', () => imageHandler(editQuillInstance));
    if (editModal._initialContent) {
      editQuillInstance.clipboard.dangerouslyPasteHTML(editModal._initialContent);
    }
  } else {
    editQuillInstance = null;
  }
});



const authHeader = () => ({ Authorization: `Bearer ${authStore.token}` });

async function loadSection(section) {
  section.loading = true;
  try {
    const { data } = await axios.post('/api/notice/list',
      { noticeType: section.type },
      { headers: authHeader() }
    );
    section.items = data;
  } catch {
    section.items = [];
  } finally {
    section.loading = false;
  }
}

onMounted(() => sections.forEach(s => loadSection(s)));

function openCreateModal(type) {
  createModal.noticeType = type;
  createModal.title = '';
  createModal.isFixed = false;
  createModal.submitting = false;
  createModal.visible = true;
}

function closeCreateModal() {
  createModal.visible = false;
}

async function submitCreate() {
  const content = quillInstance?.root?.innerHTML?.trim() ?? '';
  const isEmpty = content === '' || content === '<p><br></p>';
  if (!createModal.title.trim()) { alert('제목을 입력해주세요.'); return; }
  if (isEmpty) { alert('내용을 입력해주세요.'); return; }

  const noticeType = createModal.noticeType;
  createModal.submitting = true;
  try {
    await axios.post('/api/notice/register', {
      noticeType,
      title: createModal.title.trim(),
      content,
      isFixed: createModal.isFixed ? 'Y' : 'N',
    }, { headers: authHeader() });
    closeCreateModal();
    const section = sections.find(s => s.type === noticeType);
    if (section) loadSection(section);
  } catch (e) {
    alert(e.response?.data || '등록 중 오류가 발생했습니다.');
  } finally {
    createModal.submitting = false;
  }
}

function openEditModal(item, section) {
  editModal.noticeId = item.noticeId;
  editModal.noticeType = item.noticeType;
  editModal.title = item.title;
  editModal.isFixed = item.isFixed === 'Y';
  editModal._initialContent = item.content || '';
  editModal._section = section;
  editModal.submitting = false;
  editModal.visible = true;
}

function closeEditModal() {
  editModal.visible = false;
}

async function submitEdit() {
  const content = editQuillInstance?.root?.innerHTML?.trim() ?? '';
  const isEmpty = content === '' || content === '<p><br></p>';
  if (!editModal.title.trim()) { alert('제목을 입력해주세요.'); return; }
  if (isEmpty) { alert('내용을 입력해주세요.'); return; }

  const targetSection = editModal._section;
  editModal.submitting = true;
  try {
    await axios.post('/api/notice/update', {
      noticeId: editModal.noticeId,
      title: editModal.title.trim(),
      content,
      isFixed: editModal.isFixed ? 'Y' : 'N',
    }, { headers: authHeader() });
    closeEditModal();
    if (targetSection) loadSection(targetSection);
    else sections.forEach(s => loadSection(s));
  } catch (e) {
    alert(e.response?.data || '수정 중 오류가 발생했습니다.');
  } finally {
    editModal.submitting = false;
  }
}

function openViewModal(item) {
  viewModal.item = item;
  viewModal.visible = true;
}

function closeViewModal() {
  viewModal.visible = false;
}

async function deleteNotice(item, section) {
  if (!confirm(`"${item.title}" 게시글을 삭제하시겠습니까?`)) return;
  try {
    await axios.post('/api/notice/remove',
      { noticeId: item.noticeId },
      { headers: authHeader() }
    );
    loadSection(section);
  } catch (e) {
    alert(e.response?.data || '삭제 중 오류가 발생했습니다.');
  }
}
</script>

<style scoped>
.notice-page {
  padding: 0;
}

.bento-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.bento-card {
  background: white;
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.03);
  transition: transform 0.2s ease;
}

.bento-card:hover {
  transform: translateY(-3px);
}

/* ─── 카드 헤더 ─── */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-header h3 {
  font-size: 1.05rem;
  font-weight: 700;
  color: #333;
}

.add-btn {
  background: #2563eb;
  color: white;
  border: none;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  font-size: 20px;
  line-height: 1;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
  flex-shrink: 0;
}

.add-btn:hover { background: #1d4ed8; }

/* ─── 테이블 ─── */
.data-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.data-table th {
  text-align: left;
  padding: 10px 8px;
  font-size: 0.82rem;
  color: #999;
  border-bottom: 1px solid #eee;
  font-weight: 500;
}

.col-action { width: 130px; }

.data-row {
  cursor: pointer;
  transition: background 0.15s;
}

.data-row:hover { background: #f0f9ff; }

.data-table td {
  padding: 12px 8px;
  font-size: 0.88rem;
  border-bottom: 1px solid #f9f9f9;
  color: #444;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.title-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}

.date-cell { color: #999; font-size: 0.82rem; }
.action-cell {
  text-align: center;
  overflow: visible;
  white-space: nowrap;
}

.empty-cell {
  text-align: center;
  color: #ccc;
  padding: 20px 8px !important;
  font-size: 0.88rem;
}

.fixed-badge {
  background: #dbeafe;
  color: #2563eb;
  font-size: 0.72rem;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: 4px;
  white-space: nowrap;
  flex-shrink: 0;
}

.edit-btn {
  background: #eff6ff;
  color: #2563eb;
  border: 1px solid #bfdbfe;
  border-radius: 6px;
  padding: 4px 10px;
  font-size: 0.78rem;
  cursor: pointer;
  transition: background 0.2s;
  white-space: nowrap;
  margin-right: 4px;
}

.edit-btn:hover { background: #bfdbfe; }

.del-btn {
  background: #fff0f0;
  color: #e53935;
  border: 1px solid #ffcdd2;
  border-radius: 6px;
  padding: 4px 10px;
  font-size: 0.78rem;
  cursor: pointer;
  transition: background 0.2s;
  white-space: nowrap;
}

.del-btn:hover { background: #ffcdd2; }

/* ─── 모달 공통 ─── */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-box {
  background: white;
  border-radius: 20px;
  padding: 32px;
  width: 70vw;
  max-width: 70vw;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  max-height: 90vh;
  overflow-y: auto;
}

.modal-title {
  font-size: 1.15rem;
  font-weight: 700;
  color: #222;
  margin-bottom: 24px;
}

.form-group { margin-bottom: 18px; }

.form-group label {
  display: block;
  font-size: 0.88rem;
  font-weight: 600;
  color: #555;
  margin-bottom: 6px;
}

.required { color: #e53935; }

.form-input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #ddd;
  border-radius: 10px;
  font-size: 0.92rem;
  outline: none;
  transition: border-color 0.2s;
  box-sizing: border-box;
}

.form-input:focus { border-color: #2563eb; }

/* ─── Quill 에디터 커스터마이징 ─── */
:deep(.quill-editor-wrap .ql-toolbar) {
  border-radius: 10px 10px 0 0;
  border-color: #ddd;
  background: #fafafa;
}

:deep(.quill-editor-wrap .ql-container) {
  border-radius: 0 0 10px 10px;
  border-color: #ddd;
  font-size: 0.92rem;
}

:deep(.quill-editor-wrap .ql-editor) {
  min-height: 300px;
  max-height: 50vh;
  overflow-y: auto;
  font-family: inherit;
  font-size: 0.92rem;
  line-height: 1.7;
}

:deep(.quill-editor-wrap .ql-editor img) {
  max-width: 100%;
  height: auto;
  border-radius: 6px;
}

:deep(.quill-editor-wrap .ql-editor.ql-blank::before) {
  color: #bbb;
  font-style: normal;
}

.checkbox-row { margin-bottom: 8px; }

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.9rem;
  color: #555;
  cursor: pointer;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 24px;
}

.btn-cancel {
  padding: 10px 22px;
  border: 1px solid #ddd;
  border-radius: 10px;
  background: white;
  color: #666;
  font-size: 0.92rem;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-cancel:hover { background: #f5f5f5; }

.btn-submit {
  padding: 10px 22px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 0.92rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-submit:hover:not(:disabled) { background: #1d4ed8; }
.btn-submit:disabled { opacity: 0.6; cursor: not-allowed; }

/* ─── 보기 모달 ─── */
.modal-view { width: 720px; }

.view-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
  gap: 12px;
}

.view-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.view-title-row h3 {
  font-size: 1.1rem;
  font-weight: 700;
  color: #222;
  word-break: break-word;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.1rem;
  color: #999;
  cursor: pointer;
  padding: 2px 6px;
  border-radius: 6px;
  flex-shrink: 0;
  transition: background 0.2s;
}

.close-btn:hover { background: #f0f0f0; color: #333; }

.view-meta {
  display: flex;
  gap: 20px;
  font-size: 0.83rem;
  color: #999;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 16px;
}

/* Quill snow 스타일을 view에서도 재활용 */
.view-content-wrap {
  border: 1px solid #eee;
  border-radius: 10px;
  overflow: hidden;
}

.view-content {
  min-height: 80px;
  max-height: 480px;
  overflow-y: auto;
  padding: 16px 20px;
  font-size: 0.92rem;
  line-height: 1.75;
}

:deep(.view-content img) {
  max-width: 100%;
  height: auto;
  border-radius: 6px;
}
</style>
