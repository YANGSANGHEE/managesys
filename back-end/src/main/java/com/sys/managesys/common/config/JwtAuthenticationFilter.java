package com.sys.managesys.common.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;

    /** 로그인·로그아웃·해시생성만 JWT 검증 제외. /api/auth/change-password 는 인증 필요. */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (!uri.startsWith("/api/auth/")) return false;
        return "/api/auth/login".equals(uri) || "/api/auth/logout".equals(uri) || uri.startsWith("/api/auth/generate-hash");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {

            String token = header.substring(7);

            if (jwtProvider.validateToken(token)) {
                Boolean mustChange = jwtProvider.getMustChangePasswordFromToken(token);
                String uri = request.getRequestURI();
                boolean allowChangeOrLogout = "/api/auth/change-password".equals(uri) || "/api/auth/logout".equals(uri);
                if (Boolean.TRUE.equals(mustChange) && !allowChangeOrLogout) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("{\"message\":\"비밀번호 재설정 후 이용 가능합니다.\"}");
                    response.setContentType("application/json;charset=UTF-8");
                    return;
                }
                Long userId = jwtProvider.getUserId(token);
                UserDetails userDetails = userDetailsService.loadUserById(userId);
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}
