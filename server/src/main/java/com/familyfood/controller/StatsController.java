package com.familyfood.controller;

import com.familyfood.common.Result;
import com.familyfood.security.UserContext;
import com.familyfood.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/dashboard")
    public Result<?> dashboard() {
        return Result.ok(statsService.getDashboard(
                UserContext.getFamilyId(), UserContext.getUserId()));
    }

    @PostMapping("/rotate-chef")
    public Result<?> rotateChef() {
        statsService.rotateChef(UserContext.getFamilyId());
        return Result.ok();
    }
}
