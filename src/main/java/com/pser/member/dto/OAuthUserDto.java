package com.pser.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OAuthUserDto {
    @NotBlank
    @Email
    private String email;

    private String name;

    private String strategyCode;
}