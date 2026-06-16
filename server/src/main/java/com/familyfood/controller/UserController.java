package com.familyfood.controller;

import com.familyfood.common.Result;
import com.familyfood.security.UserContext;
import com.familyfood.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public Result<?> profile() {
        return Result.ok(userService.getProfile(UserContext.getUserId()));
    }

    @PutMapping("/profile")
    public Result<?> updateProfile(@RequestBody Map<String, String> body) {
        return Result.ok(userService.updateProfile(
                UserContext.getUserId(),
                body.get("nickname"),
                body.get("avatarUrl")
        ));
    }
}
