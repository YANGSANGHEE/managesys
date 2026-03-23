<template>
  <div class="modal-overlay">
    <div class="modal-box">
      <h3 class="modal-title">비밀번호 재설정</h3>
      <p class="modal-desc">비밀번호가 초기화된 계정입니다. 사용할 새 비밀번호를 입력해 주세요.</p>
      <form @submit.prevent="onSubmit" class="form">
        <div class="input-group">
          <label for="newPassword">새 비밀번호</label>
          <input
            id="newPassword"
            v-model="form.newPassword"
            type="password"
            placeholder="대문자·소문자·숫자·특수문자 포함 8자 이상"
            autocomplete="new-password"
            class="custom-input"
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
            class="custom-input"
          />
        </div>
        <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
        <div class="modal-actions">
          <button type="submit" class="btn-submit" :disabled="submitting">재설정 완료</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { useAuthStore } from '@/store/auth';

const authStore = useAuthStore();
const router = useRouter();

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
  const pwRegex = /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[!@#$%^&*()_\-+={}[\];':"\\|,.<>/?`~]).{8,}$/;
  if (!pwRegex.test(form.newPassword)) {
    errorMsg.value = '비밀번호는 8자 이상이며 영문 대문자, 소문자, 숫자, 특수문자를 각각 포함해야 합니다.';
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
      {
        headers: token ? { Authorization: `Bearer ${token}` } : {}
      }
    );
    authStore.logout();
    alert('비밀번호가 변경되었습니다. 새 비밀번호로 다시 로그인해 주세요.');
    router.replace('/login');
  } catch (err) {
    const msg = err.response?.data?.message || err.message || '비밀번호 변경에 실패했습니다.';
    errorMsg.value = msg;
  } finally {
    submitting.value = false;
  }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}
.modal-box {
  background: #fff;
  border-radius: 12px;
  padding: 28px 32px;
  min-width: 360px;
  max-width: 90vw;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}
.modal-title {
  margin: 0 0 8px 0;
  font-size: 1.25rem;
  font-weight: 700;
  color: #111827;
}
.modal-desc {
  margin: 0 0 24px 0;
  font-size: 0.9rem;
  color: #6b7280;
  line-height: 1.5;
}
.form {
  display: flex;
  flex-direction: column;
  gap: 16px;
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
.custom-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 1rem;
  box-sizing: border-box;
}
.custom-input:focus {
  outline: none;
  border-color: #2563eb;
}
.error-msg {
  margin: 0;
  font-size: 0.875rem;
  color: #dc2626;
}
.modal-actions {
  margin-top: 8px;
}
.btn-submit {
  width: 100%;
  padding: 12px 16px;
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
}
.btn-submit:hover:not(:disabled) {
  background: #1d4ed8;
}
.btn-submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
</style>
