<template>
  <div class="modal-overlay" @click.self="$emit('cancel')">
    <div class="modal-box">
      <div class="modal-header">
        <h3 class="modal-title">비밀번호 변경</h3>
        <button class="btn-close" @click="$emit('cancel')">✕</button>
      </div>
      <form @submit.prevent="onSubmit" class="form">
        <div class="input-group">
          <label for="currentPassword">현재 비밀번호</label>
          <input
            id="currentPassword"
            v-model="form.currentPassword"
            type="password"
            placeholder="현재 비밀번호 입력"
            autocomplete="current-password"
            class="custom-input"
          />
        </div>
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
            placeholder="새 비밀번호 재입력"
            autocomplete="new-password"
            class="custom-input"
          />
        </div>
        <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
        <div class="modal-actions">
          <button type="button" class="btn-cancel" @click="$emit('cancel')">취소</button>
          <button type="submit" class="btn-submit" :disabled="submitting">변경 완료</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
/* global defineEmits */
import { ref, reactive } from 'vue';
import axios from 'axios';
import { useAuthStore } from '@/store/auth';
import { useRouter } from 'vue-router';

defineEmits(['cancel']);

const authStore = useAuthStore();
const router = useRouter();

const form = reactive({
  currentPassword: '',
  newPassword: '',
  newPasswordConfirm: ''
});

const errorMsg = ref('');
const submitting = ref(false);

const pwRegex = /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[!@#$%^&*()_\-+={}[\];':"\\|,.<>/?`~]).{8,}$/;

async function onSubmit() {
  errorMsg.value = '';
  if (!form.currentPassword) {
    errorMsg.value = '현재 비밀번호를 입력해 주세요.';
    return;
  }
  if (!form.newPassword) {
    errorMsg.value = '새 비밀번호를 입력해 주세요.';
    return;
  }
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
    await axios.post('/api/auth/change-my-password', {
      currentPassword: form.currentPassword,
      newPassword: form.newPassword,
      newPasswordConfirm: form.newPasswordConfirm
    });

    alert('비밀번호가 변경되었습니다.\n보안을 위해 다시 로그인해 주세요.');
    await axios.post('/api/auth/logout').catch(() => {});
    authStore.logout();
    router.push('/login');
  } catch (err) {
    errorMsg.value = err.response?.data?.message || '비밀번호 변경에 실패했습니다.';
  } finally {
    submitting.value = false;
  }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}
.modal-box {
  background: #fff;
  border-radius: 12px;
  padding: 28px 32px;
  min-width: 380px;
  max-width: 90vw;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.modal-title {
  margin: 0;
  font-size: 1.2rem;
  font-weight: 700;
  color: #111827;
}
.btn-close {
  background: none;
  border: none;
  font-size: 1.1rem;
  color: #9ca3af;
  cursor: pointer;
  padding: 2px 6px;
  border-radius: 4px;
}
.btn-close:hover {
  color: #374151;
  background: #f3f4f6;
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
  font-size: 0.95rem;
  box-sizing: border-box;
  transition: border-color 0.2s;
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
  display: flex;
  gap: 10px;
  margin-top: 4px;
}
.btn-cancel {
  flex: 1;
  padding: 11px 16px;
  background: #f3f4f6;
  color: #374151;
  border: none;
  border-radius: 8px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
}
.btn-cancel:hover {
  background: #e5e7eb;
}
.btn-submit {
  flex: 2;
  padding: 11px 16px;
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 0.95rem;
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
