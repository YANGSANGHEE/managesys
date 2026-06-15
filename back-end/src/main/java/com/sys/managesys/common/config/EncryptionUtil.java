package com.sys.managesys.common.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 개인정보 양방향 암호화 유틸 (AES-256-GCM).
 * - 저장 시 encrypt, 조회 시 decrypt. (서버에서만 키 보유)
 * - 키: app.encryption.key (Base64 인코딩된 32바이트). 운영은 환경변수/외부 키스토어 주입 권장.
 * - 암호문은 "enc:v1:" prefix 로 식별 → 평문/암호문 구분(과도기 호환) + 재암호화 방지(멱등성).
 * - MyBatis TypeHandler 가 static 으로 호출하므로 @PostConstruct 에서 static 키를 초기화.
 */
@Component
public class EncryptionUtil {

    private static final String TRANSFORM = "AES/GCM/NoPadding";
    private static final int IV_LEN = 12;        // GCM 권장 IV 길이(바이트)
    private static final int TAG_BITS = 128;     // GCM 인증 태그 길이(비트)
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ENC_PREFIX = "enc:v1:";

    private static SecretKeySpec KEY;

    @Value("${app.encryption.key:}")
    private String configuredKey;

    @PostConstruct
    void init() {
        if (configuredKey == null || configuredKey.isBlank()) {
            throw new IllegalStateException(
                    "app.encryption.key 가 설정되지 않았습니다. (Base64 인코딩된 32바이트 AES-256 키 필요)");
        }
        byte[] keyBytes = Base64.getDecoder().decode(configuredKey.trim());
        if (keyBytes.length != 32) {
            throw new IllegalStateException(
                    "app.encryption.key 는 32바이트(AES-256)여야 합니다. 현재: " + keyBytes.length + "바이트");
        }
        KEY = new SecretKeySpec(keyBytes, "AES");
    }

    /** 평문 → 암호문(enc:v1:Base64(IV||CT||TAG)). null/빈문자/이미암호문은 그대로 반환. */
    public static String encrypt(String plain) {
        if (plain == null || plain.isEmpty()) return plain;
        if (isEncrypted(plain)) return plain;           // 멱등성
        try {
            byte[] iv = new byte[IV_LEN];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORM);
            cipher.init(Cipher.ENCRYPT_MODE, KEY, new GCMParameterSpec(TAG_BITS, iv));
            byte[] ct = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            byte[] out = new byte[iv.length + ct.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(ct, 0, out, iv.length, ct.length);
            return ENC_PREFIX + Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new IllegalStateException("개인정보 암호화 실패", e);
        }
    }

    /** 암호문 → 평문. null/빈문자/평문(prefix 없음)은 그대로 반환(과도기 호환). */
    public static String decrypt(String stored) {
        if (stored == null || stored.isEmpty()) return stored;
        if (!isEncrypted(stored)) return stored;        // 평문이면 그대로
        try {
            byte[] data = Base64.getDecoder().decode(stored.substring(ENC_PREFIX.length()));
            byte[] iv = new byte[IV_LEN];
            System.arraycopy(data, 0, iv, 0, IV_LEN);
            byte[] ct = new byte[data.length - IV_LEN];
            System.arraycopy(data, IV_LEN, ct, 0, ct.length);
            Cipher cipher = Cipher.getInstance(TRANSFORM);
            cipher.init(Cipher.DECRYPT_MODE, KEY, new GCMParameterSpec(TAG_BITS, iv));
            return new String(cipher.doFinal(ct), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("개인정보 복호화 실패", e);
        }
    }

    /**
     * 암호문 여부 판별. prefix 뿐 아니라 payload 가 유효한 Base64 이고
     * 최소 길이(IV + GCM 태그)를 만족해야 암호문으로 본다.
     * → 'enc:v1:' 로 시작하는 평문(우연/공격)을 암호문으로 오판하는 위험을 줄인다.
     */
    public static boolean isEncrypted(String v) {
        if (v == null || !v.startsWith(ENC_PREFIX)) return false;
        try {
            byte[] data = Base64.getDecoder().decode(v.substring(ENC_PREFIX.length()));
            return data.length >= IV_LEN + (TAG_BITS / 8);   // IV + 최소 GCM 태그 길이
        } catch (IllegalArgumentException e) {
            return false;   // 유효한 Base64 가 아니면 평문으로 취급
        }
    }
}
