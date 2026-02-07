
package com.sys.managesys.common.config;

import com.sys.managesys.common.dto.UserDto;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {
    private final UserDto user;

    public CustomUserDetails(UserDto user) {
        this.user = user;
    }

    public Long getUserId() {
        return user.getUserId();
    }

    public String getRoleCode() {
        return user.getDeptId();
    }

    @Override
    public String getUsername() {
        return user.getLoginId();
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
        return List.of(new SimpleGrantedAuthority(user.getDeptId()));
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
}
