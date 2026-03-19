-- TB_USER에 비밀번호 초기화 여부 컬럼 추가 (초기화된 계정은 로그인 시 재설정 유도)
ALTER TABLE TB_USER
  ADD COLUMN IF NOT EXISTS PASSWORD_RESET_YN CHAR(1) DEFAULT 'N' COMMENT '비밀번호 초기화 여부 (Y: 로그인 시 재설정 필요)';

-- 기존 데이터는 N 유지 (DEFAULT 'N' 적용)
