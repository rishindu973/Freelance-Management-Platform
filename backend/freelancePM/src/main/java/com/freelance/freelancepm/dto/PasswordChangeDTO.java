package com.freelance.freelancepm.dto;

import lombok.Data;

@Data
public class PasswordChangeDTO {
    private String token;
    private String newPassword;
}
