package com.familyfood.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.familyfood.common.BizException;
import com.familyfood.entity.User;
import com.familyfood.mapper.UserMapper;
import com.familyfood.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public Map<String, Object> wxLogin(String code, String nickname, String avatarUrl) {
        // 开发阶段：用 code 作为 openid（正式环境需调用微信接口）
        String openid = "wx_" + code;
        if (code == null || code.isEmpty()) {
            throw new BizException("登录凭证不能为空");
        }

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getOpenid, openid));

        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setNickname(nickname != null ? nickname : "用户" + System.currentTimeMillis() % 10000);
            user.setAvatarUrl(avatarUrl);
            userMapper.insert(user);
        }

        String token = jwtUtil.generateToken(user.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("nickname", user.getNickname());
        result.put("avatarUrl", user.getAvatarUrl());
        return result;
    }
}
