package com.sys.managesys.common.mapper;

import com.sys.managesys.common.dto.UserDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {

    // --- [1. 인증 관련] ---

    // 로그인 시 (LOGIN_ID 기준)
    UserDto findByLoginId(@Param("loginId") String loginId);

    // JWT 인증 시 (USER_ID 기준)
    UserDto findByUserId(@Param("userId") Long userId);

    // --- [2. 인사 관리 CRUD] ---

    // 직원 전체 조회 (검색 조건 포함)
    List<UserDto> selectAllUsers(UserDto userDto);

    // 직원 등록
    int insertUser(UserDto userDto);

    // 직원 수정
    int updateUser(UserDto userDto);

    // 직원 삭제 (물리 삭제 시)
    int deleteUser(@Param("userId") Long userId);

    // --- [3. 추가 기능: 중복 체크 및 부서 팝업] ---

    // 아이디 중복 확인 (로그인 ID가 몇 개 있는지 카운트)
    int countByLoginId(@Param("loginId") String loginId);

    // 부서 목록 조회 (팝업용 - 부서명 검색 필터 포함)
    List<Map<String, Object>> selectDepartments(@Param("deptName") String deptName);
}