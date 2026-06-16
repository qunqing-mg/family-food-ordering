package com.familyfood.dto;

import lombok.Data;

@Data
public class WxLoginRequest {
    private String code;
    private String nickname;
    private String avatarUrl;
}
