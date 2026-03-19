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
        if ("MEMBER".equals(userDetails.getRoleCode())) {
            return ResponseEntity.status(403).body("권한이 없습니다.");
        }
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

    /** 삭제 - ADMIN / MANAGER 이면서 자신이 작성한 글만 */
    @PostMapping("/remove")
    public ResponseEntity<?> remove(@RequestBody Map<String, Object> body,
                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        if ("MEMBER".equals(userDetails.getRoleCode())) {
            return ResponseEntity.status(403).body("권한이 없습니다.");
        }
        Long noticeId = Long.parseLong(body.get("noticeId").toString());
        Long userId = userDetails.getUserId();
        int cnt = noticeMapper.countByNoticeIdAndCreator(noticeId, userId);
        if (cnt == 0) {
            return ResponseEntity.status(403).body("자신이 작성한 글만 삭제할 수 있습니다.");
        }
        noticeMapper.deleteNotice(noticeId, userId);
        return ResponseEntity.ok().build();
    }
}
