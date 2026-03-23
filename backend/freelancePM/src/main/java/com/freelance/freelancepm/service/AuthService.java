package com.freelance.freelancepm.service;

import com.freelance.freelancepm.dto.AuthResponseDTO;
import com.freelance.freelancepm.dto.LoginDTO;
import com.freelance.freelancepm.entity.User;
import com.freelance.freelancepm.repository.UserRepository;
import com.freelance.freelancepm.security.CustomUserDetailsService;
import com.freelance.freelancepm.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final IEmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponseDTO login(LoginDTO loginDTO) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getEmail(),
                        loginDTO.getPassword()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(loginDTO.getEmail());
        String jwt = jwtUtil.generateToken(userDetails);

        User user = userRepository.findByEmail(loginDTO.getEmail()).orElseThrow();

        return new AuthResponseDTO(jwt, user.getEmail(), user.getRole());
    }

    @Override
    public void requestPasswordReset(String email) {
        // Find user by email. If not found, do nothing silently (prevents email
        // enumeration).
        userRepository.findByEmail(email).ifPresent(user -> {
            String resetToken = java.util.UUID.randomUUID().toString();
            user.setResetPasswordToken(resetToken);
            user.setResetPasswordExpires(java.time.LocalDateTime.now().plusHours(1));
            userRepository.save(user);

            emailService.sendPasswordResetEmail(email, resetToken);
            System.out.println("Password reset token generated for: " + email);
        });
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token or user not found"));

        if (user.getResetPasswordExpires() == null
                || user.getResetPasswordExpires().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("Reset link expired");
        }

        // Successfully verified. Hash new password.
        user.setPassword(passwordEncoder.encode(newPassword));

        // Explicitly invalidate token so it can never be used again.
        user.setResetPasswordToken(null);
        user.setResetPasswordExpires(null);
        userRepository.save(user);
    }
}
