import { createRouter, createWebHistory } from 'vue-router'
import NoticeEle from '@/components/NoticeEle.vue'
import MainPage from '@/components/MainPage.vue'
import UserManagement from '@/components/UserManagement.vue'
// 로그인 컴포넌트를 임포트하세요 (파일 경로는 본인의 프로젝트에 맞게 수정)
import LoginView from '@/views/LoginView.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
    meta: {
      title: '로그인'
      , isPublic: true
    }
  },
  {
    path: '/notice',
    name: 'Notice',
    component: NoticeEle,
    meta: { title: '공지사항' }
  },
  {
    path: '/userManagement',
    name: 'userManagement',
    component: UserManagement,
    meta: { title: '인사관리' }
  },
  {
    path: '/customers',
    name: 'Customers',
    component: MainPage,
    meta: { title: '고객관리' }
  },
  {
    path: '/targets',
    name: 'Targets',
    component: () => import('@/components/PlaceholderPage.vue'),
    meta: { title: '가망고객' }
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

// 네비게이션 가드 추가 (이동할 때마다 실행)
router.beforeEach((to, from, next) => {
  // 토큰이 있는지 확인
  const isAuthenticated = localStorage.getItem('accessToken');

  // 로그인 페이지로 가는 게 아닌데, 토큰이 없다면?
  if (to.name !== 'Login' && !isAuthenticated) {
    next({ name: 'Login' }); // 로그인 페이지로 튕겨내기
  } else {
    next(); // 통과
  }
});

export default router