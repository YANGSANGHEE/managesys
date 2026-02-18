<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { useAuthStore } from '@/store/auth'; // 1. 스토어 임포트

const router = useRouter();
const authStore = useAuthStore();

// 백엔드 LoginRequest DTO와 필드명을 일치시켜야 함 (userId -> loginId)
const loginData = ref({
  loginId: '',
  password: ''
});

const handleLogin = async () => {
  if (!loginData.value.loginId || !loginData.value.password) {
    alert("아이디와 비밀번호를 입력해주세요.");
    return;
  }

  try {
    // 백엔드 로그인 API 호출
    const response = await axios.post('/api/auth/login', loginData.value);

// 1. 응답 데이터 받기
    const data = response.data;

    if (data.accessToken) {
      // 2. 토큰 저장
      localStorage.setItem('accessToken', data.accessToken);

      // 3. [핵심] 사용자 정보 객체로 묶어서 저장
      const userInfo = {
        userId: data.userId,
        loginId: data.loginId,
        userName: data.userName,
        userRole: data.userRole,
        deptId: data.deptId,
        isLeader: data.isLeader
      };

      authStore.login(data.accessToken, userInfo);

      router.push('/');
    }
  } catch (error) {
    console.error(error);
    alert('로그인 실패: 아이디 혹은 비밀번호를 확인하세요.');
  }
};
</script>

<template>
  <div class="login-split-container flex">
    <div class="brand-panel flex-col justify-between">
<!--      <div class="brand-logo">-->
<!--        <span class="logo-icon">로고가들어가야함</span>-->
<!--      </div>-->
      <div class="brand-message">
        <h1>Welcome<br>Back!</h1>
        <p>회사소개글,<br>회사명</p>
      </div>
      <div class="brand-footer">
        © 2024 회사명 Corp. All rights reserved.
      </div>
    </div>

    <div class="form-panel flex-center">
      <div class="form-box">
        <h2>로그인</h2>
        <p class="form-desc">계정에 로그인하여 서비스를 이용하세요.</p>

        <form class="flex-col gap-20" @submit.prevent="handleLogin">
          <div class="input-group">
            <label for="userId">아이디</label>
            <input type="text" id="userId" v-model="loginData.loginId" placeholder="아이디를 입력하세요" class="custom-input">
          </div>
          <div class="input-group">
            <label for="password">비밀번호</label>
            <input type="password" v-model="loginData.password" id="password" placeholder="비밀번호를 입력하세요" class="custom-input">
          </div>

          <button type="submit" class="login-btn-lg">로그인</button>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 전체 컨테이너: 좌우 분할 레이아웃 */
.login-split-container {
  min-height: 100vh;
  width: 100%;
}

/* 왼쪽 브랜드 패널 */
.brand-panel {
  width: 40%;
  background-color: #3d5afe;/* 기존 포인트 컬러 유지 */
  color: white;
  padding: 60px;
}
.brand-logo {
  font-size: 1.5rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 10px;
}
.logo-icon {
  background: white;
  color: #3d5afe;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  font-weight: 900;
}
.brand-message h1 {
  font-size: 4rem;
  font-weight: 800;
  line-height: 1.1;
  margin-bottom: 20px;
}
.brand-message p {
  font-size: 1.1rem;
  opacity: 0.9;
  line-height: 1.6;
}
.brand-footer {
  font-size: 0.9rem;
  opacity: 0.7;
}

/* 오른쪽 폼 패널 */
.form-panel {
  width: 60%;
  background-color: #ffffff;
  padding: 40px;
}
.form-box {
  width: 100%;
  max-width: 440px;
}
.form-box h2 {
  font-size: 2rem;
  font-weight: 700;
  color: #111827;
  margin-bottom: 10px;
}
.form-desc {
  color: #6b7280;
  margin-bottom: 40px;
}

/* 폼 요소 스타일 */
.input-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 20px;
}
.input-group label {
  font-weight: 600;
  font-size: 0.95rem;
  color: #374151;
}
.custom-input {
  width: 100%;
  padding: 14px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.2s;
}
.custom-input:focus {
  outline: none;
  border-color:#3d5afe;
}

/* 추가 옵션 (체크박스, 링크 등) */
.form-options {
  font-size: 0.9rem;
  color: #4b5563;
}
.remember-me {
  gap: 8px;
  cursor: pointer;
}
.forgot-pw, .signup-link {
  color: #3d5afe;
  font-weight: 600;
  text-decoration: none;
}

/* 로그인 버튼 */
.login-btn-lg {
  width: 100%;
  padding: 16px;
  background-color: #3d5afe;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1.1rem;
  font-weight: 700;
  cursor: pointer;
  transition: background-color 0.2s;
}
.login-btn-lg:hover {
  background-color: #4338ca;
}

/* 하단 푸터 */
.form-footer {
  margin-top: 30px;
  font-size: 0.95rem;
  color: #6b7280;
}

/* 반응형 처리 (모바일에서는 위아래로 배치) */
@media (max-width: 1024px) {
  .login-split-container {
    flex-direction: column;
  }
  .brand-panel, .form-panel {
    width: 100%;
  }
  .brand-panel {
    padding: 40px 30px;
    min-height: 300px;
  }
  .brand-message h1 {
    font-size: 3rem;
  }
  .form-panel {
    padding: 50px 30px;
  }
}
</style>