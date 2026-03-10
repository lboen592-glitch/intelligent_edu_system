package com.demo_system.config;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.demo_system.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    // 跳过 ASYNC / ERROR 派发
    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return true;
    }
    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return true;
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // 放行登录注册
        if (path.equals("/api/login") || path.equals("/api/register")) return true;
        // 放行静态资源
        if (path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/")) return true;
        if (!path.startsWith("/api/")) return true;
        return false;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 放行预检请求
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            response.setStatus(401);
            return;
        }
        String token = auth.substring(7).trim();
        try {
            DecodedJWT jwt = JwtUtil.verify(token);
            Long userId = Long.valueOf(jwt.getSubject());
            String username = jwt.getClaim("username").asString();
            // 把 userId/username 放入 SecurityContext（后续接口可直接获取）
            var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
            var authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            // 如仍然想用 requestAttribute顺手塞一下
            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            response.setStatus(401);
            return;
        }
    }
}
