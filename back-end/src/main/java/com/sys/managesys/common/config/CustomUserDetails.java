
package com.sys.managesys.common.config;

import com.sys.managesys.common.dto.UserDto;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

public class CustomUserDetails implements UserDetails {
    private final UserDto user;

    public CustomUserDetails(UserDto user) {
        this.user = user;
    }

    public Long getUserId() {
        return user.getUserId();
    }

    public String getRoleCode() {
        return user.getUserRole();
    }

    public int getIsLeader() {
        return user.getIsLeader();
    }

    public long getDeptId() {
        return user.getDeptId();
    }

    @Override
    public String getUsername() {
        return user.getUserName();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public boolean isEnabled() {
        return "Y".equals(user.getUseYn());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collectors = new ArrayList<>();

        // 1. DB에서 가져온 Role 값 확인
        String role = user.getUserRole(); // 혹은 getRole(), 필드명에 맞게 수정

        // 2. Role이 없으면 기본값 부여 (Null 방지)
        if (role == null || role.trim().isEmpty()) {
            role = "ROLE_MEMBER"; // 기본 권한 설정
        }

        // 3. Spring Security는 보통 "ROLE_" 접두사를 좋아함 (선택사항이나 추천)
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }

        collectors.add(new SimpleGrantedAuthority(role));
        return collectors;
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }

    public UserDto getUserDto() {
        return user;
    }
}
