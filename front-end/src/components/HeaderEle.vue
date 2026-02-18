<template>
  <header class="content-header">
    <h1>{{ currentMenu }}</h1>
    <div class="flex justify-between items-center">
      <div class="user-info">
        <b>{{ authStore.userName }}</b>님
      </div>
      <button @click="handleLogout" class="logout-btn">로그아웃</button>
    </div>
  </header>
</template>
<script setup>
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/store/auth';
const router = useRouter();
const authStore = useAuthStore();
// 로그아웃 처리 함수
const handleLogout = () => {
  if (confirm('로그아웃 하시겠습니까?')) {
    // 1. 저장된 토큰 삭제
    localStorage.removeItem('accessToken');
    authStore.logout();
    // 2. 로그인 페이지로 이동
    // (이때 App.vue의 조건문에 의해 사이드바도 같이 사라집니다)
    router.push('/login');
  }
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


/*로그아웃*/
.logout-btn {
  background: transparent;
  border: none;
  color: #6b7280;       /* gray-500 느낌 */
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