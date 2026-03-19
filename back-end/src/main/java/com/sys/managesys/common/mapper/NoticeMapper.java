package com.sys.managesys.common.mapper;

import com.sys.managesys.common.dto.NoticeDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NoticeMapper {
    List<NoticeDto> selectByType(String noticeType);
    void insertNotice(NoticeDto dto);
    void deleteNotice(@Param("noticeId") Long noticeId, @Param("creatorId") Long creatorId);
    int countByNoticeIdAndCreator(@Param("noticeId") Long noticeId, @Param("creatorId") Long creatorId);
}
