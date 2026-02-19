package com.sys.managesys.common.auth.controller;

import com.sys.managesys.common.auth.service.NoticeService;
import com.sys.managesys.common.dto.NoticeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    // 공지사항 목록 조회
    @GetMapping("/list")
    public List<NoticeDto> getNoticeList() {
        return noticeService.getNoticeList();
    }

    @PostMapping("/register")
    public void registerNotice(@RequestBody NoticeDto noticeDto) {
        noticeService.registerNotice(noticeDto);
    }

    @PostMapping("/update")
    public void updateNotice(@RequestBody NoticeDto noticeDto) {
        noticeService.updateNotice(noticeDto);
    }

    @PostMapping("/delete/{noticeId}")
    public void deleteNotice(@PathVariable Integer noticeId) {
        noticeService.deleteNotice(noticeId);
    }

    @PostMapping("/delete-batch")
    public void deleteNoticeBatch(@RequestBody Map<String, List<Integer>> params) {
        noticeService.deleteNoticeBatch(params.get("noticeIds"));
    }
}