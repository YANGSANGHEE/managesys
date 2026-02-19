<template>

  <div class="bento-grid">

    <div v-if="isModalOpen" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>{{ selectedNotice.isFixed === 'Y' ? '[필독] ' : '' }}{{ selectedNotice.title }}</h2>
          <button @click="closeModal" class="close-x">&times;</button>
        </div>
        <div class="modal-info">
          <span><strong>작성자:</strong> {{ selectedNotice.creatorName }}</span>
          <span><strong>작성일:</strong> {{ selectedNotice.createdAt?.substring(0, 10) }}</span>
          <span><strong>조회수:</strong> {{ selectedNotice.viewCnt }}</span>
        </div>
        <hr>
        <div class="modal-body">
          <div class="content-text">{{ selectedNotice.content }}</div>
        </div>
        <div class="modal-footer">
          <button @click="prepareUpdate" class="edit-btn">수정</button>
          <button @click="confirmDelete" class="delete-btn">삭제</button>
          <button @click="closeModal" class="confirm-btn">확인</button>
        </div>
      </div>
    </div>

    <div v-if="isWriteModalOpen" class="modal-overlay" @click.self="isWriteModalOpen = false">
      <div class="modal-content">
        <div class="modal-header">
          <h2>{{ newNotice.noticeType === 'INTERNAL' ? '사내' : '협력점' }} 공지사항 작성</h2>
        </div>
        <div class="modal-body">
          <div class="type-selection">
            <label><input type="radio" v-model="newNotice.noticeType" value="INTERNAL"> 사내</label>
            <label><input type="radio" v-model="newNotice.noticeType" value="PARTNER"> 협력점</label>
          </div>

          <input v-model="newNotice.title" class="input-title" placeholder="제목을 입력하세요" />
          <div class="fix-check">
            <input type="checkbox" v-model="newNotice.isFixed" true-value="Y" false-value="N" id="isFixed" />
            <label for="isFixed">상단 고정</label>
          </div>
          <textarea v-model="newNotice.content" class="input-content" placeholder="내용을 입력하세요"></textarea>
        </div>
        <div class="modal-footer">
          <button @click="isWriteModalOpen = false" class="cancel-btn">취소</button>
          <button @click="submitNotice" class="confirm-btn">저장</button>
        </div>
      </div>
    </div>

    <section v-for="section in sections" :key="section.title" class="bento-card">
      <div class="card-header">
        <h3>{{ section.title }}</h3>
        <div class="header-btns">
          <button class="batch-delete-btn" @click="deleteSelected(section.data)">선택 삭제</button>
          <button class="more-btn" @click="openWriteModal(section.type)">+</button>
        </div>
      </div>

      <table class="data-table">
        <thead>
        <tr>
          <th><input type="checkbox" @change="toggleAll(section, $event)"></th> <th>제목</th>
          <th>등록자</th>
          <th>등록일</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="item in section.data" :key="item.noticeId" class="clickable-row">
          <td>
            <input type="checkbox" v-model="item.selected" @click.stop>
          </td>
          <td class="title-cell" @click="openDetail(item)">{{ item.title }}</td>
          <td>{{ item.creatorName }}</td>
          <td>{{ item.createdAt?.substring(0, 10) }}</td>
        </tr>
        <tr v-if="section.data.length === 0">
          <td colspan="3" class="empty-cell">내용이 없습니다.</td>
        </tr>
        </tbody>
      </table>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

const sections = ref([
  { title: '사내 공지사항', type: 'INTERNAL', data: [] },
  { title: '협력점 공지사항', type: 'PARTNER', data: [] },
  { title: '사내 자료실', type: 'INTERNAL_FILE', data: [] },
  { title: '협력점 자료실', type: 'PARTNER_FILE', data: [] },
]);

// 상세보기 관련 상태 추가
const isModalOpen = ref(false);
const isWriteModalOpen = ref(false);
const selectedNotice = ref({});
const newNotice = ref({
  title: '',
  content: '',
  isFixed: 'N',
  noticeType: 'INTERNAL' // 기본값 INTERNAL(사내)
});

// 데이터 가져오기 및 분류
const fetchNotices = async () => {
  try {
    const response = await axios.get('http://localhost:8085/api/notice/list');
    const allData = response.data; // 전체 데이터 (사내 + 협력점)

    // 사내 공지사항 (noticeType = 'INTERNAL')
    // sections.value[0].data = allNotices.filter(item => item.noticeType === 'INTERNAL');
    sections.value[0].data = allData
        .filter(n => n.noticeType === 'INTERNAL')
        .map(n => ({ ...n, selected: false })); // selected 속성 강제 주입

    // 협력점 공지사항 (noticeType = 'PARTNER')
    // sections.value[1].data = allNotices.filter(item => item.noticeType === 'PARTNER');
    sections.value[1].data = allData
        .filter(n => n.noticeType === 'PARTNER')
        .map(n => ({ ...n, selected: false }));

  } catch (error) {
    console.error('공지사항 로드 실패:', error);
  }
};

// 전체 선택/해제
const toggleAll = (section, event) => {
  const isChecked = event.target.checked;
  section.data.forEach(item => {
    item.selected = isChecked;
  });
};

// 상세 보기
const openDetail = (item) => {
  selectedNotice.value = item;
  isModalOpen.value = true;
  // (선택사항) 여기서 조회수 증가 API를 나중에 호출할 수 있습니다.
};

// 모달 닫기
const closeModal = () => {
  isModalOpen.value = false;
};

// 등록 팝업 열기 (타입 주입)
const openWriteModal = (type) => {
  newNotice.value = {
    title: '',
    content: '',
    isFixed: 'N',
    noticeType: type // 버튼을 누른 섹션의 타입이 자동으로 설정됨
  };
  isWriteModalOpen.value = true;
};

// 삭제
const confirmDelete = async () => {
  if (!confirm('정말 이 공지사항을 삭제하시겠습니까?')) return;

  try {
    // 백엔드에 삭제 요청 (Delete 메서드 혹은 Post 사용)
    await axios.post(`http://localhost:8085/api/notice/delete/${selectedNotice.value.noticeId}`);
    alert('삭제되었습니다.');
    isModalOpen.value = false; // 상세 모달 닫기
    await fetchNotices();      // 목록 새로고침
  } catch (error) {
    console.error('삭제 실패:', error);
    alert('삭제 중 오류가 발생했습니다.');
  }
};

// 수정
const isUpdateMode = ref(false); // 수정인지 신규등록인지 구분용
const prepareUpdate = () => {
  isUpdateMode.value = true;
  // 상세 내용 데이터를 작성 팝업 데이터로 복사
  newNotice.value = { ...selectedNotice.value };
  isModalOpen.value = false;      // 상세창 닫고
  isWriteModalOpen.value = true;  // 작성(수정)창 열기
};

// 선택 삭제
const deleteSelected = async (dataList) => {
  // selected가 true인 것들의 noticeId만 뽑기
  const selectedIds = dataList
      .filter(item => item.selected)
      .map(item => item.noticeId);

  if (selectedIds.length === 0) {
    alert('삭제할 항목을 선택해주세요.');
    return;
  }

  if (!confirm(`선택한 ${selectedIds.length}건을 삭제하시겠습니까?`)) return;

  try {
    // 여러 ID를 배열로 보냄
    await axios.post('http://localhost:8085/api/notice/delete-batch', {
      noticeIds: selectedIds
    });
    alert('삭제되었습니다.');
    await fetchNotices();
  } catch (error) {
    console.error('일괄 삭제 실패:', error);
  }
};

// 저장
const submitNotice = async () => {
  if (!newNotice.value.title || !newNotice.value.content) {
    alert('제목과 내용을 입력해주세요.');
    return;
  }

  const url = isUpdateMode.value
      ? 'http://localhost:8085/api/notice/update'
      : 'http://localhost:8085/api/notice/register';

  try {
    await axios.post(url, newNotice.value);
    alert(isUpdateMode.value ? '수정되었습니다.' : '등록되었습니다.');
    isWriteModalOpen.value = false;
    isUpdateMode.value = false; // 모드 초기화
    await fetchNotices();
  } catch (error) {
    console.error('저장 실패:', error);
  }
};

onMounted(() => {
  fetchNotices();
});

</script>

<style scoped>

.bento-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
  margin-bottom: 40px;
}

.bento-card {
  background: white;
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.03);
  transition: transform 0.2s ease;
}

.bento-card:hover {
  transform: translateY(-5px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header h3 {
  font-size: 1.1rem;
  color: #333;
}

.more-btn {
  background: #f0f2f5;
  border: none;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  cursor: pointer;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th {
  text-align: left;
  padding: 12px 8px;
  font-size: 0.85rem;
  color: #888;
  border-bottom: 1px solid #eee;
}

.data-table td {
  padding: 14px 8px;
  font-size: 0.9rem;
  border-bottom: 1px solid #f9f9f9;
}

.empty-cell {
  color: #ccc;
  text-align: center;
}

/* 클릭 가능한 행 스타일 */
.clickable-row {
  cursor: pointer;
  transition: background 0.2s;
}
.clickable-row:hover {
  background-color: #f8f9fa;
}
.title-cell {
  font-weight: 500;
  color: #2c3e50;
}

/* 모달 오버레이 (뒷배경) */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

/* 모달 창 */
.modal-content {
  background: white;
  padding: 30px;
  border-radius: 15px;
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 10px 25px rgba(0,0,0,0.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.modal-info {
  display: flex;
  gap: 20px;
  font-size: 0.85rem;
  color: #666;
  margin-bottom: 15px;
}

.modal-body {
  padding: 20px 0;
  min-height: 150px;
}

.content-text {
  white-space: pre-wrap; /* DB의 줄바꿈(\n)을 화면에 그대로 표시 */
  line-height: 1.6;
  color: #444;
}

.confirm-btn {
  background: #4e73df;
  color: white;
  border: none;
  padding: 10px 25px;
  border-radius: 8px;
  cursor: pointer;
  float: right;
}

.close-x {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #aaa;
}

.input-title {
  width: 100%;
  padding: 12px;
  margin-bottom: 10px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 1rem;
}

.input-content {
  width: 100%;
  height: 200px;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  resize: none;
  font-family: inherit;
}

.fix-check {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.9rem;
}

.cancel-btn {
  background: #eee;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  margin-right: 10px;
}

.edit-btn {
  background: #f6ad55;
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 5px;
  cursor: pointer;
  margin-right: 5px;
}

.delete-btn {
  background: #fc8181;
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 5px;
  cursor: pointer;
  margin-right: auto;
}

.header-btns {
  display: flex;
  gap: 8px;
  align-items: center;
}

.batch-delete-btn {
  background: #fff;
  color: #ff4d4f;
  border: 1px solid #ff4d4f;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
}

.batch-delete-btn:hover {
  background: #fff1f0;
}

</style>