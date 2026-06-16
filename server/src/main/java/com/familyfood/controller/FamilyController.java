package com.familyfood.controller;

import com.familyfood.common.Result;
import com.familyfood.security.UserContext;
import com.familyfood.service.FamilyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/family")
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyService familyService;

    @PostMapping
    public Result<?> create(@RequestBody Map<String, String> body) {
        Long userId = UserContext.getUserId();
        return Result.ok(familyService.createFamily(userId, body.get("name")));
    }

    @PostMapping("/join")
    public Result<?> join(@RequestBody Map<String, String> body) {
        Long userId = UserContext.getUserId();
        return Result.ok(familyService.joinFamily(userId, body.get("inviteCode")));
    }

    @GetMapping("/members")
    public Result<?> members() {
        return Result.ok(familyService.getMembers(UserContext.getFamilyId()));
    }

    @GetMapping("/info")
    public Result<?> info() {
        return Result.ok(familyService.getFamilyInfo(UserContext.getFamilyId()));
    }

    @DeleteMapping("/leave")
    public Result<?> leave() {
        familyService.leaveFamily(UserContext.getUserId(), UserContext.getFamilyId());
        return Result.ok();
    }

    @PutMapping("/settings")
    public Result<?> updateSettings(@RequestBody Map<String, String> body) {
        familyService.updateSettings(UserContext.getFamilyId(), body.get("orderDeadline"));
        return Result.ok();
    }
}
