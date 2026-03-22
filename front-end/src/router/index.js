import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import NoticeEle from '@/components/NoticeEle.vue'
import UserManagement from '@/components/UserManagement.vue'
import LoginView from '@/views/LoginView.vue'
import ResetPasswordView from '@/views/ResetPasswordView.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
    meta: { title: '로그인', isPublic: true }
  },
  {
    path: '/reset-password',
    name: 'ResetPassword',
    component: ResetPasswordView,
    meta: { title: '비밀번호 재설정', isResetPassword: true }
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
    redirect: { name: 'CustomersIndividual' }
  },
  {
    path: '/customers/individual',
    name: 'CustomersIndividual',
    component: () => import('@/components/IndividualCustomerManagement.vue'),
    meta: { title: '개인고객관리' }
  },
  {
    path: '/customers/partner',
    name: 'CustomersPartner',
    component: () => import('@/components/PartnerCustomerManagement.vue'),
    meta: { title: '협력점 고객관리' }
  },
  {
    path: '/targets',
    name: 'Targets',
    component: () => import('@/components/PlaceholderPage.vue'),
    meta: { title: '가망고객' }
  },
  {
    path: '/commonCode',
    name: 'CommonCode',
    component: () => import('@/components/CommonCodeManagement.vue'),
    meta: { title: '공통코드관리' }
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

/** JWT exp claim 체크 (서명 검증 없이 만료 여부만 확인) */
function isTokenExpired(token) {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return Date.now() >= payload.exp * 1000
  } catch {
    return true
  }
}

// 라우터 가드: 비로그인 → /login, 공통코드 ADMIN 전용
// mustChangePassword 처리: App.vue 전역 모달이 담당 (페이지 이동 불필요)
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  authStore.setLoading(true)

  const token = localStorage.getItem('accessToken')
  const hasToken = !!token && !isTokenExpired(token)

  if (!hasToken) {
    authStore.logout()
  } else {
    authStore.rehydrate()
  }

  const user = authStore.user
  const isLoginPath = to.name === 'Login'

  if (!hasToken) {
    if (!isLoginPath) next({ name: 'Login' })
    else next()
    return
  }
  // 인사관리: 최고관리자(ADMIN)만 접근 가능
  if (to.path === '/userManagement' && user?.userRole !== 'ADMIN') {
    next({ path: '/notice' })
    return
  }
  // 공통코드관리: 최고관리자(ADMIN), 매니저(MANAGER)만 접근 가능
  if (to.path === '/commonCode' && user?.userRole !== 'ADMIN' && user?.userRole !== 'MANAGER') {
    next({ path: '/notice' })
    return
  }
  next()
})

router.afterEach(() => {
  const authStore = useAuthStore()
  // 페이지 전환 후 로딩 해제 (axios 요청이 없는 페이지를 위해 약간의 딜레이)
  setTimeout(() => authStore.setLoading(false), 300)
})

export default router