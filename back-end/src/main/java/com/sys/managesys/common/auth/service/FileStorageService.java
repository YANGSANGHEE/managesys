package com.sys.managesys.common.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * 로컬 디스크 기반 파일 저장 서비스.
 * 파일 실체는 app.upload.cust-file-dir 경로에 UUID 이름으로 저장하고,
 * 메타정보(원본명/경로 등)는 TB_CUST_FILE 에 별도 보관한다.
 */
@Service
public class FileStorageService {

    @Value("${app.upload.cust-file-dir:C:/managesys/uploads/customer}")
    private String uploadDir;

    /** 허용 확장자 (이미지/PDF) */
    private static final java.util.Set<String> ALLOWED_EXT =
            java.util.Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp", "pdf");

    /** 파일당 최대 크기 (10MB) */
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    private Path baseDir;

    @PostConstruct
    public void init() {
        try {
            baseDir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new IllegalStateException("업로드 디렉토리를 생성할 수 없습니다: " + uploadDir, e);
        }
    }

    /** 저장 결과 (저장명 + 절대경로) */
    public record StoredFile(String storedFileName, String absolutePath) {}

    /**
     * 파일을 디스크에 저장하고 저장명/경로를 반환한다.
     */
    public StoredFile store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("빈 파일은 업로드할 수 없습니다.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기는 10MB 이하만 업로드할 수 있습니다.");
        }
        String origin = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String ext = extractExtension(origin);
        if (!ALLOWED_EXT.contains(ext.toLowerCase())) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다. (이미지/PDF만 가능)");
        }
        String stored = UUID.randomUUID().toString().replace("-", "") + (ext.isEmpty() ? "" : "." + ext);
        Path target = baseDir.resolve(stored).normalize();
        // 경로 탈출 방지
        if (!target.startsWith(baseDir)) {
            throw new IllegalArgumentException("잘못된 파일 경로입니다.");
        }
        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("파일 저장에 실패했습니다: " + origin, e);
        }
        return new StoredFile(stored, target.toString());
    }

    /** 디스크에서 파일을 Resource 로 로드 */
    public Resource loadAsResource(String absolutePath) {
        try {
            Path path = Paths.get(absolutePath).normalize();
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalArgumentException("파일을 찾을 수 없습니다.");
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("파일 경로가 올바르지 않습니다.", e);
        }
    }

    /** 디스크 파일 삭제 (실패해도 예외 전파하지 않음 — 메타 삭제가 우선) */
    public void deleteQuietly(String absolutePath) {
        if (absolutePath == null) return;
        try {
            Files.deleteIfExists(Paths.get(absolutePath).normalize());
        } catch (IOException ignored) {
            // 디스크 파일이 이미 없거나 잠겨 있어도 무시
        }
    }

    private String extractExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return (dot >= 0 && dot < filename.length() - 1) ? filename.substring(dot + 1) : "";
    }
}
