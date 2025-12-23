package com.sys.managesys.common.config;

import com.sys.managesys.common.dto.UserDto;
import com.sys.managesys.common.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService{
    private final UserMapper userMapper;

    // 로그인 시 사용
    @Override
    public UserDetails loadUserByUsername(String loginId) {
        UserDto user = userMapper.findByLoginId(loginId);

        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        return new CustomUserDetails(user);
    }
    public UserDetails loadUserById(Long userId) {

        UserDto user = userMapper.findByUserId(userId);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        return new CustomUserDetails(user);
    }
}
