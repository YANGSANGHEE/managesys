<template>
  <header class="content-header">
    <h1>{{ currentMenu }}</h1>
    <div class="flex justify-between items-center">
      <div class="user-info">
        <button class="user-name-btn" @click="showPasswordModal = true" title="비밀번호 변경">
          <b>{{ authStore.userName }}</b>님
          <span class="pw-hint">🔑</span>
        </button>
      </div>
      <button @click="handleLogout" class="logout-btn">로그아웃</button>
    </div>
  </header>

  <MyPasswordModal v-if="showPasswordModal" @cancel="showPasswordModal = false" />
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/store/auth';
import axios from 'axios';
import MyPasswordModal from '@/components/MyPasswordModal.vue';

const router = useRouter();
const authStore = useAuthStore();

const showPasswordModal = ref(false);

const handleLogout = () => {
  if (!confirm('로그아웃 하시겠습니까?')) return;
  axios.post('/api/auth/logout')
    .finally(() => {
      authStore.logout();
      router.push('/login');
    });
};
</script>

<style scoped>
.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.content-header h1 {
  font-size: 1.8rem;
  color: #444;
}

/* 유저명 버튼 */
.user-name-btn {
  background: none;
  border: none;
  font-size: inherit;
  color: #374151;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: background-color 0.15s;
}
.user-name-btn:hover {
  background-color: #f3f4f6;
}
.pw-hint {
  font-size: 0.8rem;
  opacity: 0.5;
}
.user-name-btn:hover .pw-hint {
  opacity: 1;
}

/* 로그아웃 */
.logout-btn {
  background: transparent;
  border: none;
  color: #6b7280;
  font-size: 13px;
  padding: 4px 6px;
  border-radius: 6px;
  cursor: pointer;
}
.logout-btn:hover {
  color: #111827;
  background-color: #f3f4f6;
}
</style>
