package com.sys.managesys.common.service;

import com.sys.managesys.common.dto.LoginHistoryDto;
import com.sys.managesys.common.mapper.LoginHistoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 로그인 히스토리 분기별 자동 정리 서비스
 * - 매 분기 첫째 날 00:00 에 실행 (1월, 4월, 7월, 10월 1일)
 * - 3개월 이전 데이터를 CSV 덤프 후 DB에서 삭제
 * - 덤프 경로: application.properties → app.login-history.dump-path
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginHistoryCleanupService {

    private final LoginHistoryMapper loginHistoryMapper;

    @Value("${app.login-history.dump-path}")
    private String dumpPath;

    private static final DateTimeFormatter FILE_DT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter CSV_DT  = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 분기 첫째 날 00:00 실행 (1/4/7/10월 1일)
     * cron: 초 분 시 일 월 요일
     */
    @Scheduled(cron = "0 0 0 1 1,4,7,10 *")
    public void cleanupLoginHistory() {
        LocalDateTime before = LocalDateTime.now().minusMonths(3);
        log.info("[LoginHistoryCleanup] 정리 시작 - {} 이전 데이터 대상", before);

        List<LoginHistoryDto> rows = loginHistoryMapper.findOlderThan(before);
        if (rows.isEmpty()) {
            log.info("[LoginHistoryCleanup] 삭제할 데이터 없음. 종료.");
            return;
        }

        try {
            writeCsv(rows, before);
        } catch (IOException e) {
            log.error("[LoginHistoryCleanup] 덤프 파일 저장 실패 - 삭제 중단: {}", e.getMessage());
            return; // 덤프 실패 시 DB 삭제하지 않음 (데이터 보호)
        }

        int deleted = loginHistoryMapper.deleteOlderThan(before);
        log.info("[LoginHistoryCleanup] 완료 - {}건 삭제", deleted);
    }

    private void writeCsv(List<LoginHistoryDto> rows, LocalDateTime before) throws IOException {
        Path dir = Paths.get(dumpPath);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
            log.info("[LoginHistoryCleanup] 덤프 디렉토리 생성: {}", dir);
        }

        String fileName = "login_history_" + before.format(FILE_DT) + ".csv";
        Path file = dir.resolve(fileName);

        try (BufferedWriter bw = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            // BOM (Excel에서 한글 깨짐 방지)
            bw.write('\uFEFF');
            bw.write("HISTORY_ID,USER_ID,EVENT_TYPE,IP_ADDRESS,USER_AGENT,CREATED_AT");
            bw.newLine();

            for (LoginHistoryDto row : rows) {
                bw.write(String.join(",",
                        nullSafe(row.getHistoryId()),
                        nullSafe(row.getUserId()),
                        csvEscape(row.getAction()),
                        csvEscape(row.getIpAddress()),
                        csvEscape(row.getUserAgent()),
                        row.getCreatedAt() != null ? row.getCreatedAt().format(CSV_DT) : ""
                ));
                bw.newLine();
            }
        }

        log.info("[LoginHistoryCleanup] 덤프 저장 완료: {} ({}건)", file, rows.size());
    }

    private String nullSafe(Object val) {
        return val == null ? "" : val.toString();
    }

    /** 쉼표/큰따옴표 포함 값 CSV 이스케이프 */
    private String csvEscape(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }
}
