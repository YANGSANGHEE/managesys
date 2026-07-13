package com.sys.managesys.common.controller;

import com.sys.managesys.common.config.CustomUserDetails;
import com.sys.managesys.common.dto.NoticeDto;
import com.sys.managesys.common.mapper.NoticeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeMapper noticeMapper;

    /**
     * 공지 쓰기(등록/수정/삭제) 권한 검증 - 관리자(ADMIN)·팀장(MANAGER)만 허용(allow-list).
     * 권한이 없으면 403 ResponseEntity 를, 있으면 null 을 반환한다.
     * (deny-list("MEMBER"만 차단) + userDetails 미인증 시 NPE 위험을 함께 해소)
     */
    private ResponseEntity<?> checkWritePermission(CustomUserDetails userDetails) {
        String role = userDetails != null ? userDetails.getRoleCode() : null;
        if (!"ADMIN".equals(role) && !"MANAGER".equals(role)) {
            return ResponseEntity.status(403).body("권한이 없습니다.");
        }
        return null;
    }

    /** 전체 조회 - 모든 로그인 사용자 */
    @PostMapping("/list")
    public List<NoticeDto> list(@RequestBody Map<String, Object> body) {
        String noticeType = body.getOrDefault("noticeType", "COMPANY").toString();
        return noticeMapper.selectByType(noticeType);
    }

    /** 등록 - ADMIN / MANAGER 만 */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody NoticeDto dto,
                                      @AuthenticationPrincipal CustomUserDetails userDetails) {
        ResponseEntity<?> denied = checkWritePermission(userDetails);
        if (denied != null) return denied;
        if (dto.getTitle() == null || dto.getTitle().isBlank())
            return ResponseEntity.badRequest().body("제목을 입력해주세요.");
        if (dto.getContent() == null || dto.getContent().isBlank())
            return ResponseEntity.badRequest().body("내용을 입력해주세요.");
        if (dto.getNoticeType() == null || dto.getNoticeType().isBlank())
            return ResponseEntity.badRequest().body("공지유형이 올바르지 않습니다.");
        dto.setCreatorId(userDetails.getUserId());
        noticeMapper.insertNotice(dto);
        return ResponseEntity.ok().build();
    }

    /** 수정 - ADMIN / MANAGER 이면서 자신이 작성한 글만 */
    @PostMapping("/update")
    public ResponseEntity<?> update(@RequestBody NoticeDto dto,
                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        ResponseEntity<?> denied = checkWritePermission(userDetails);
        if (denied != null) return denied;
        if (dto.getTitle() == null || dto.getTitle().isBlank())
            return ResponseEntity.badRequest().body("제목을 입력해주세요.");
        if (dto.getContent() == null || dto.getContent().isBlank())
            return ResponseEntity.badRequest().body("내용을 입력해주세요.");
        dto.setCreatorId(userDetails.getUserId());
        int cnt = noticeMapper.countByNoticeIdAndCreator(dto.getNoticeId(), dto.getCreatorId());
        if (cnt == 0) {
            return ResponseEntity.status(403).body("자신이 작성한 글만 수정할 수 있습니다.");
        }
        noticeMapper.updateNotice(dto);
        return ResponseEntity.ok().build();
    }

    /** 삭제 - ADMIN은 모든 글, MANAGER는 자신이 작성한 글만 */
    @PostMapping("/remove")
    public ResponseEntity<?> remove(@RequestBody Map<String, Object> body,
                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        ResponseEntity<?> denied = checkWritePermission(userDetails);
        if (denied != null) return denied;
        Long noticeId = Long.parseLong(body.get("noticeId").toString());
        Long userId = userDetails.getUserId();
        boolean isAdmin = "ADMIN".equals(userDetails.getRoleCode());
        if (!isAdmin) {
            int cnt = noticeMapper.countByNoticeIdAndCreator(noticeId, userId);
            if (cnt == 0) {
                return ResponseEntity.status(403).body("자신이 작성한 글만 삭제할 수 있습니다.");
            }
        }
        noticeMapper.deleteNoticeById(noticeId);
        return ResponseEntity.ok().build();
    }
}
