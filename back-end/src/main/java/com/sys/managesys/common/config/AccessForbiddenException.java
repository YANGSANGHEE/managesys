package com.sys.managesys.common.config;

/**
 * 인가(권한) 실패 전용 예외. → HTTP 403 Forbidden 으로 매핑된다.
 * 입력 검증 실패(IllegalArgumentException → 400)와 구분하기 위해 도입.
 */
public class AccessForbiddenException extends RuntimeException {
    public AccessForbiddenException(String message) {
        super(message);
    }
}
