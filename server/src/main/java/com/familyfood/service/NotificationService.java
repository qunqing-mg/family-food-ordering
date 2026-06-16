package com.familyfood.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.familyfood.entity.*;
import com.familyfood.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notiMapper;
    private final FamilyMemberMapper memberMapper;

    public void notifyFamily(Long familyId, Long fromUserId, String type, String content) {
        List<FamilyMember> members = memberMapper.selectList(
                new LambdaQueryWrapper<FamilyMember>().eq(FamilyMember::getFamilyId, familyId));
        for (FamilyMember m : members) {
            if (m.getUserId().equals(fromUserId)) continue;
            Notification n = new Notification();
            n.setFamilyId(familyId); n.setUserId(m.getUserId());
            n.setType(type); n.setContent(content);
            n.setIsRead(0); n.setCreateTime(LocalDateTime.now());
            notiMapper.insert(n);
        }
    }

    public int getUnreadCount(Long userId) {
        Long count = notiMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, 0));
        return count != null ? count.intValue() : 0;
    }

    public List<Notification> getUnreadList(Long userId) {
        return notiMapper.selectList(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, 0)
                        .orderByDesc(Notification::getCreateTime)
                        .last("LIMIT 20"));
    }

    public void markAllRead(Long userId) {
        List<Notification> list = notiMapper.selectList(
                new LambdaQueryWrapper<Notification>().eq(Notification::getUserId, userId));
        for (Notification n : list) {
            n.setIsRead(1); notiMapper.updateById(n);
        }
    }
}
