package com.sys.managesys.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 기존 평문 개인정보를 일괄 암호화하는 1회성 마이그레이션.
 * - app.encryption.migrate-on-startup=true 일 때만 애플리케이션 시작 시 실행.
 * - 멱등: 이미 'enc:v1:' 로 시작하는 값은 건너뜀 → 여러 번 실행해도 안전.
 * - TypeHandler 를 우회(이중암호화 방지)하기 위해 JdbcTemplate(raw SQL)로 직접 처리.
 * 운영 적용 후에는 플래그를 false 로 되돌리는 것을 권장.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EncryptionMigrationRunner implements ApplicationRunner {

    private final JdbcTemplate jdbc;

    @Value("${app.encryption.migrate-on-startup:false}")
    private boolean enabled;

    /** (테이블, PK 컬럼, 암호화 대상 컬럼) */
    private static final String[][] TARGETS = {
            {"TB_CUSTOMER",     "CUST_ID", "SSN_ENC"},
            {"TB_CUSTOMER",     "CUST_ID", "FOREIGNER_REG_NO"},
            {"TB_CUST_PAYMENT", "CUST_ID", "ACCOUNT_CARD_NO"},
            {"TB_CUST_PAYMENT", "CUST_ID", "HOLDER_SSN_ENC"},
            {"TB_CUST_GIFT",    "GIFT_ID", "HOLDER_SSN_ENC"},
            {"TB_CUST_GIFT",    "GIFT_ID", "ACCOUNT_NO"},
            {"TB_CUST_MNP",     "MNP_ID",  "OWNER_SSN_ENC"},
    };

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled) {
            log.info("[encrypt-migration] 비활성(app.encryption.migrate-on-startup=false) - 건너뜀");
            return;
        }
        int total = 0;
        for (String[] t : TARGETS) {
            total += migrate(t[0], t[1], t[2]);
        }
        log.info("[encrypt-migration] 완료: 총 {}건 암호화", total);
    }

    private int migrate(String table, String pk, String col) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT " + pk + " AS id, " + col + " AS val FROM " + table +
                " WHERE " + col + " IS NOT NULL AND " + col + " <> ''");
        int cnt = 0;
        for (Map<String, Object> r : rows) {
            Object valObj = r.get("val");
            if (valObj == null) continue;
            String val = valObj.toString();
            if (EncryptionUtil.isEncrypted(val)) continue;     // 이미 암호문
            String enc = EncryptionUtil.encrypt(val);
            jdbc.update("UPDATE " + table + " SET " + col + " = ? WHERE " + pk + " = ?", enc, r.get("id"));
            cnt++;
        }
        if (cnt > 0) log.info("[encrypt-migration] {}.{}: {}건 암호화", table, col, cnt);
        return cnt;
    }
}
