/**
 * 고객관리 권한 정책 회귀 테스트 (API E2E, 무의존성)
 * ----------------------------------------------------------------------------
 * 검증 대상 (커밋 a2446b3 / 보강분):
 *   - 일반 담당자(MEMBER): 조회 O, 등록(Create) O, 수정/삭제 X
 *   - 최고관리자(ADMIN)·팀장(MANAGER): 전체 CRUD
 *   - quickUpdate(그리드 인라인 수정)도 isWriter + 부서범위 검증
 *
 * 실행:
 *   node tests/customer-permission.api.test.mjs
 *
 * 환경변수(선택, 기본값 존재):
 *   BASE_URL   기본 http://localhost:18085   (운영 포트면 http://localhost:8085)
 *   ADMIN_ID / ADMIN_PW   기본 admin / Admin1234!
 *   SEED=1     admin 권한으로 MEMBER/MANAGER 테스트 계정 비밀번호를 초기화·정리하여
 *              스크립트를 독립 실행 가능하게 만든다. (테스트 DB 전용! 운영 금지)
 *   MEMBER_ID / MEMBER_PW, MANAGER_ID / MANAGER_PW
 *              SEED 미사용 시 직접 지정. SEED 사용 시 자동 탐색·세팅.
 *
 * 주의: 테스트는 임시 고객을 생성 후 삭제하고, 성공 케이스(수정)는 현재 값과
 *       동일하게(멱등) 호출하여 실데이터를 변경하지 않는다.
 *
 * 종료코드: 모든 단언 통과 시 0, 하나라도 실패 시 1.
 */

const BASE = (process.env.BASE_URL || 'http://localhost:18085').replace(/\/$/, '');
const ADMIN = { id: process.env.ADMIN_ID || 'admin', pw: process.env.ADMIN_PW || 'Admin1234!' };
const SEED = process.env.SEED === '1';
const TEST_PW = 'Test1234!'; // 시딩 시 MEMBER/MANAGER에 설정할 임시 비밀번호 (8자+영문+숫자+특수)

let pass = 0, fail = 0;
const results = [];
function check(name, cond, detail = '') {
  if (cond) { pass++; results.push(`  PASS  ${name}`); }
  else { fail++; results.push(`  FAIL  ${name}${detail ? ` — ${detail}` : ''}`); }
}

async function api(method, path, token, body) {
  const headers = { 'Content-Type': 'application/json' };
  if (token) headers.Authorization = `Bearer ${token}`;
  const res = await fetch(`${BASE}${path}`, {
    method, headers, body: body !== undefined ? JSON.stringify(body) : undefined,
  });
  let json = null;
  const text = await res.text();
  try { json = text ? JSON.parse(text) : null; } catch { json = text; }
  return { status: res.status, json };
}

async function login(id, pw) {
  const r = await api('POST', '/api/auth/login', null, { loginId: id, password: pw });
  return r.json && r.json.accessToken ? r.json : null;
}

/** SEED 모드: admin 으로 역할별 계정을 찾아 비밀번호를 초기화→변경하여 알려진 값으로 맞춘다. */
async function seedAccount(adminTok, role) {
  const list = await api('POST', '/api/admin/users/list', adminTok, {});
  const u = (list.json || []).find(x => x.userRole === role && x.useYn === 'Y');
  if (!u) throw new Error(`${role} 역할 테스트 계정을 찾을 수 없습니다.`);
  await api('POST', '/api/admin/users/reset-password', adminTok, { userId: u.userId }); // pw = {loginId}1234!
  const resetTok = (await login(u.loginId, `${u.loginId}1234!`))?.accessToken;
  if (!resetTok) throw new Error(`${u.loginId} 초기화 비밀번호 로그인 실패`);
  await api('POST', '/api/auth/change-password', resetTok, { newPassword: TEST_PW, newPasswordConfirm: TEST_PW });
  return { id: u.loginId, pw: TEST_PW };
}

async function main() {
  console.log(`[고객관리 권한 회귀 테스트] target=${BASE}\n`);

  // 0) admin 로그인
  const adminLogin = await login(ADMIN.id, ADMIN.pw);
  if (!adminLogin) { console.error('admin 로그인 실패 — BASE_URL/계정을 확인하세요.'); process.exit(1); }
  const adminTok = adminLogin.accessToken;

  // 0-1) MEMBER / MANAGER 계정 확보
  let member, manager;
  if (SEED) {
    member = await seedAccount(adminTok, 'MEMBER');
    manager = await seedAccount(adminTok, 'MANAGER');
  } else {
    member = { id: process.env.MEMBER_ID, pw: process.env.MEMBER_PW };
    manager = { id: process.env.MANAGER_ID, pw: process.env.MANAGER_PW };
    if (!member.id || !manager.id) {
      console.error('MEMBER_ID/PW, MANAGER_ID/PW 환경변수가 필요합니다. (또는 SEED=1 사용)');
      process.exit(1);
    }
  }
  const memberTok = (await login(member.id, member.pw))?.accessToken;
  const managerTok = (await login(manager.id, manager.pw))?.accessToken;
  check('MEMBER 로그인', !!memberTok);
  check('MANAGER 로그인', !!managerTok);
  if (!memberTok || !managerTok) { report(); process.exit(1); }

  // 1) READ: MEMBER 본인 배정 고객 목록 조회 가능
  const memList = await api('POST', '/api/customers/list', memberTok, {});
  check('MEMBER 목록 조회 200', memList.status === 200, `status=${memList.status}`);
  const memRows = Array.isArray(memList.json) ? memList.json : [];

  // 2) CREATE: MEMBER 등록 허용 (상품 미포함 → 임시 고객 생성 후 정리)
  const reg = await api('POST', '/api/customers/register', memberTok,
    { customer: { custName: 'PERM_TEST_MEMBER', custType: 'PERS' } });
  check('MEMBER 등록(Create) 200', reg.status === 200, `status=${reg.status}`);
  // cleanup: admin 으로 방금 생성분 삭제
  const created = await api('POST', '/api/customers/list', adminTok, { searchCustName: 'PERM_TEST_MEMBER' });
  const createdId = (created.json || [])[0]?.custId;
  if (createdId) await api('DELETE', `/api/customers/${createdId}`, adminTok);

  // 3) UPDATE/DELETE/QUICK: MEMBER 차단 (본인 배정 고객 대상 → 조회권한 통과해도 isWriter 로 차단)
  const target = memRows.find(r => r.custId);
  if (target) {
    const upd = await api('PUT', '/api/customers/update', memberTok,
      { customer: { custId: target.custId, custName: target.custName || 'X' } });
    check('MEMBER 수정(PUT) 400 차단', upd.status === 400, `status=${upd.status}`);

    const del = await api('DELETE', `/api/customers/${target.custId}`, memberTok);
    check('MEMBER 삭제 400 차단(실삭제 없음)', del.status === 400, `status=${del.status}`);

    const qu = await api('PATCH', `/api/customers/${target.custId}/quick-update`, memberTok,
      { field: 'payDone', value: '', prodId: null });
    check('MEMBER quick-update 400 차단', qu.status === 400, `status=${qu.status}`);
  } else {
    check('MEMBER 대상 고객 존재(검증 전제)', false, 'MEMBER 배정 고객 0건 — 시드 데이터 확인');
  }

  // 4) WRITE 허용: ADMIN/MANAGER quick-update (멱등: 현재 status 동일값으로 → 실변경 없음)
  //    MANAGER 본인 부서 고객 대상으로 수행해야 부서범위 검증도 통과
  const mgrList = await api('POST', '/api/customers/list', managerTok, {});
  const mgrTarget = (Array.isArray(mgrList.json) ? mgrList.json : []).find(r => r.custId && r.prodId);
  if (mgrTarget) {
    const detail = await api('GET', `/api/customers/detail?custId=${mgrTarget.custId}`, adminTok);
    const prod = (detail.json?.products || []).find(p => p.prodId === mgrTarget.prodId) || (detail.json?.products || [])[0];
    const curStatus = prod?.openStatus;
    if (curStatus && prod?.prodId) {
      const mgrQu = await api('PATCH', `/api/customers/${mgrTarget.custId}/quick-update`, managerTok,
        { field: 'status', value: curStatus, prodId: String(prod.prodId) });
      check('MANAGER quick-update(본인부서·멱등) 200', mgrQu.status === 200, `status=${mgrQu.status}`);

      const admQu = await api('PATCH', `/api/customers/${mgrTarget.custId}/quick-update`, adminTok,
        { field: 'status', value: curStatus, prodId: String(prod.prodId) });
      check('ADMIN quick-update 200', admQu.status === 200, `status=${admQu.status}`);
    } else {
      check('MANAGER 대상 상품 openStatus 확보', false, '상품/상태 미확인');
    }
  } else {
    check('MANAGER 대상 고객(상품 포함) 존재', false, 'MANAGER 부서 내 상품 보유 고객 0건');
  }

  report();
  process.exit(fail > 0 ? 1 : 0);
}

function report() {
  console.log(results.join('\n'));
  console.log(`\n결과: ${pass} PASS / ${fail} FAIL`);
}

main().catch(e => { console.error('실행 오류:', e.message); process.exit(1); });
