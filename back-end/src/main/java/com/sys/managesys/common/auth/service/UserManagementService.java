package com.sys.managesys.common.auth.service;

import com.sys.managesys.common.dto.UserDto;
import com.sys.managesys.common.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public List<UserDto> findAllUsers(UserDto searchDto) {
        return userMapper.selectAllUsers(searchDto);
    }

    @Transactional
    public void register(UserDto userDto) {
        // 비밀번호 암호화 적용
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }

        // 2. 기본값 설정 (선택사항)
        if (userDto.getUseYn() == null) userDto.setUseYn("Y");

        userMapper.insertUser(userDto);
    }

    @Transactional
    public void modify(UserDto userDto) {
        // 비밀번호 입력 시에만 새롭게 해싱하여 업데이트
        if (userDto.getPassword() != null && !userDto.getPassword().trim().isEmpty()) {
            userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
        } else {
            userDto.setPassword(null); // MyBatis에서 null 체크로 기존 비번 유지
        }
        userMapper.updateUser(userDto);
    }

    @Transactional
    public void remove(Long userId) {
        userMapper.deleteUser(userId);
    }

    public boolean isIdDuplicate(String loginId) {
        return userMapper.countByLoginId(loginId) > 0;
    }

    public List<Map<String, Object>> findDepartments(String deptName) {
        return userMapper.selectDepartments(deptName);
    }
}