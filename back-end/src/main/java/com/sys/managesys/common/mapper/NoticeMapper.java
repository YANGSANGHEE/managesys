package com.sys.managesys.common.mapper;

import com.sys.managesys.common.dto.NoticeDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NoticeMapper {
    // 공지사항 목록 조회 (작성자 이름 포함)
    List<NoticeDto> selectNoticeList();

    // 공지사항 추가
    void insertNotice(NoticeDto noticeDto);

    // 공지사항 수정
    void updateNotice(NoticeDto noticeDto);

    // 공지사항 삭제
    void deleteNotice(Integer noticeId);

    void deleteNoticeBatch(@Param("noticeIds") List<Integer> noticeIds);
}