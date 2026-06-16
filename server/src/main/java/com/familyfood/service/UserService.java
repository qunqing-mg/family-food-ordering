package com.familyfood.service;

import com.familyfood.entity.User;
import com.familyfood.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    public User getProfile(Long userId) {
        return userMapper.selectById(userId);
    }

    public User updateProfile(Long userId, String nickname, String avatarUrl) {
        User user = userMapper.selectById(userId);
        if (user == null) return null;
        if (nickname != null && !nickname.isBlank()) {
            user.setNickname(nickname);
        }
        if (avatarUrl != null && !avatarUrl.isBlank()) {
            user.setAvatarUrl(avatarUrl);
        }
        userMapper.updateById(user);
        return user;
    }
}
