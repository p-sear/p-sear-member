package com.psear.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConfirmMailRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String emailCode;
}
