package com.sys.managesys.common.auth.service;

import com.sys.managesys.common.dto.NoticeDto;
import com.sys.managesys.common.mapper.NoticeMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeMapper noticeMapper;

    // 공지사항 전체 목록 조회
    public List<NoticeDto> getNoticeList() {
        return noticeMapper.selectNoticeList();
    }

    @Transactional
    public void registerNotice(NoticeDto noticeDto) {
        // 기본값 설정
        if (noticeDto.getIsFixed() == null) noticeDto.setIsFixed("N");
        if (noticeDto.getCreatorId() == null) noticeDto.setCreatorId(1); // 임시 ID (테스트용)

        noticeMapper.insertNotice(noticeDto);
    }

    @Transactional
    public void updateNotice(NoticeDto noticeDto) {
        noticeMapper.updateNotice(noticeDto);
    }

    @Transactional
    public void deleteNotice(Integer noticeId) {
        noticeMapper.deleteNotice(noticeId);
    }

    /**
     * 공지사항 일괄 삭제 (소프트 삭제)
     * @param noticeIds 삭제할 ID 리스트
     */
    @Transactional // 여러 건을 처리하므로 트랜잭션 처리가 중요합니다.
    public void deleteNoticeBatch(List<Integer> noticeIds) {
        if (noticeIds != null && !noticeIds.isEmpty()) {
            noticeMapper.deleteNoticeBatch(noticeIds);
        }
    }

}
