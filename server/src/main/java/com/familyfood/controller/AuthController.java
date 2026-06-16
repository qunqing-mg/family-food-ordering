package com.familyfood.controller;

import com.familyfood.common.Result;
import com.familyfood.dto.WxLoginRequest;
import com.familyfood.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/wx-login")
    public Result<?> wxLogin(@RequestBody WxLoginRequest request) {
        return Result.ok(authService.wxLogin(
                request.getCode(), request.getNickname(), request.getAvatarUrl()));
    }
}
