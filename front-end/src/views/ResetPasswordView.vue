<template>
  <div class="reset-password-page">
    <div class="reset-password-box">
      <h1 class="title">비밀번호 재설정</h1>
      <p class="desc">비밀번호가 초기화된 계정입니다. 새 비밀번호를 설정한 후 다시 로그인해 주세요.</p>
      <form @submit.prevent="onSubmit" class="form">
        <div class="input-group">
          <label for="newPassword">새 비밀번호</label>
          <input
            id="newPassword"
            v-model="form.newPassword"
            type="password"
            placeholder="새 비밀번호 (4자 이상)"
            autocomplete="new-password"
            class="input"
          />
        </div>
        <div class="input-group">
          <label for="newPasswordConfirm">새 비밀번호 확인</label>
          <input
            id="newPasswordConfirm"
            v-model="form.newPasswordConfirm"
            type="password"
            placeholder="새 비밀번호 확인"
            autocomplete="new-password"
            class="input"
          />
        </div>
        <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
        <button type="submit" class="btn-submit" :disabled="submitting">재설정 완료</button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { useAuthStore } from '@/store/auth';

const router = useRouter();
const authStore = useAuthStore();

const form = reactive({
  newPassword: '',
  newPasswordConfirm: ''
});
const errorMsg = ref('');
const submitting = ref(false);

async function onSubmit() {
  errorMsg.value = '';
  if (!form.newPassword || !form.newPasswordConfirm) {
    errorMsg.value = '새 비밀번호와 확인을 모두 입력해 주세요.';
    return;
  }
  if (form.newPassword.length < 4) {
    errorMsg.value = '비밀번호는 4자 이상으로 설정해 주세요.';
    return;
  }
  if (form.newPassword !== form.newPasswordConfirm) {
    errorMsg.value = '새 비밀번호가 일치하지 않습니다.';
    return;
  }

  submitting.value = true;
  try {
    const token = localStorage.getItem('accessToken');
    await axios.post(
      '/api/auth/change-password',
      {
        newPassword: form.newPassword,
        newPasswordConfirm: form.newPasswordConfirm
      },
      { headers: token ? { Authorization: `Bearer ${token}` } : {} }
    );
    authStore.logout();
    alert('비밀번호가 변경되었습니다. 새 비밀번호로 다시 로그인해 주세요.');
    router.replace('/login');
  } catch (err) {
    errorMsg.value = err.response?.data?.message || err.message || '비밀번호 변경에 실패했습니다.';
  } finally {
    submitting.value = false;
  }
}
</script>

<style scoped>
.reset-password-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f4f7f6;
}
.reset-password-box {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  min-width: 380px;
  max-width: 90vw;
  box-shadow: 0 4px 24px rgba(0,0,0,0.08);
}
.title {
  margin: 0 0 8px 0;
  font-size: 1.5rem;
  font-weight: 700;
  color: #111827;
}
.desc {
  margin: 0 0 28px 0;
  font-size: 0.95rem;
  color: #6b7280;
  line-height: 1.5;
}
.form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.input-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.input-group label {
  font-weight: 600;
  font-size: 0.9rem;
  color: #374151;
}
.input {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 1rem;
  box-sizing: border-box;
}
.input:focus {
  outline: none;
  border-color: #3d5afe;
}
.error-msg {
  margin: 0;
  font-size: 0.875rem;
  color: #dc2626;
}
.btn-submit {
  margin-top: 8px;
  padding: 14px 20px;
  background: #3d5afe;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
}
.btn-submit:hover:not(:disabled) {
  background: #4338ca;
}
.btn-submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
</style>
