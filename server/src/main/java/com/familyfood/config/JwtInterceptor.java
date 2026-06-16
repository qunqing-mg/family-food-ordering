package com.familyfood.config;

import com.familyfood.mapper.FamilyMemberMapper;
import com.familyfood.security.JwtUtil;
import com.familyfood.security.UserContext;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final FamilyMemberMapper memberMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equals(request.getMethod())) return true;

        String path = request.getRequestURI();
        if (path.startsWith("/api/auth/")) return true; // 登录接口放行

        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(401);
            return false;
        }

        token = token.substring(7);
        if (!jwtUtil.validateToken(token)) {
            response.setStatus(401);
            return false;
        }

        Long userId = jwtUtil.getUserId(token);
        // 查询用户所在家庭
        var member = memberMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.familyfood.entity.FamilyMember>()
                        .eq(com.familyfood.entity.FamilyMember::getUserId, userId));
        Long familyId = member != null ? member.getFamilyId() : null;

        UserContext.set(userId, familyId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
