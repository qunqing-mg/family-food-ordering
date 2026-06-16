package com.familyfood.controller;

import com.familyfood.common.Result;
import com.familyfood.security.UserContext;
import com.familyfood.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/noti")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notiService;

    @GetMapping("/unread-count")
    public Result<?> unreadCount() {
        return Result.ok(notiService.getUnreadCount(UserContext.getUserId()));
    }

    @GetMapping("/unread-list")
    public Result<?> unreadList() {
        return Result.ok(notiService.getUnreadList(UserContext.getUserId()));
    }

    @PostMapping("/read-all")
    public Result<?> readAll() {
        notiService.markAllRead(UserContext.getUserId());
        return Result.ok();
    }
}
