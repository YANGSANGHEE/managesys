import { createRouter, createWebHistory } from 'vue-router'
import NoticeEle from '@/components/NoticeEle.vue'
import MainPage from '@/components/MainPage.vue'
// 로그인 컴포넌트를 임포트하세요 (파일 경로는 본인의 프로젝트에 맞게 수정)
import LoginView from '@/views/LoginView.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
    // 로그인 페이지는 인증 없이 접근 가능해야 하므로 isPublic을 true로 설정합니다.
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
    path: '/customers',
    name: 'Customers',
    component: MainPage,
    meta: { title: '고객관리' }
  },
  // ... 나머지 경로들
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

// 네비게이션 가드: 페이지 이동 전마다 실행됨
// router.beforeEach((to, from, next) => {
//   const token = localStorage.getItem('accessToken');
//
//   // 1. 가려는 페이지가 public이 아니고(인증 필요)
//   // 2. 토큰이 없다면
//   if (!to.meta.isPublic && !token) {
//     // 로그인 페이지로 강제 이동
//     next('/login');
//   } else {
//     // 그 외의 경우(토큰이 있거나, public 페이지인 경우) 정상 이동
//     next();
//   }
// });

// router.beforeEach((to, from, next) => {
//   const token = localStorage.getItem('accessToken');
//   if (to.path !== '/login' && !token) {
//     next('/login');
//   } else {
//     next();
//   }
// });

export default router