package com.psear.member.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthenticateResponse {
    private String token;

    private String refreshToken;
}