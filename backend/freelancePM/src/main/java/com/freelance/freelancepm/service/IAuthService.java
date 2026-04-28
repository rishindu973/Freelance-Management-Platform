package com.freelance.freelancepm.service;

import com.freelance.freelancepm.dto.AuthResponseDTO;
import com.freelance.freelancepm.dto.LoginDTO;

public interface IAuthService {
    AuthResponseDTO login(LoginDTO loginDTO);

    void requestPasswordReset(String email);

    void resetPassword(String token, String newPassword);
}
