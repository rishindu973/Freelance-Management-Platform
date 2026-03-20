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
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // In a real system, you'd save this token to DB with an expiration time.
        // For simplicity, we are generating a stateless token that uses the email as
        // subject.
        String resetToken = jwtUtil.generateToken(userDetailsService.loadUserByUsername(email));

        emailService.sendPasswordResetEmail(email, resetToken);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid token or user not found"));

        if (!jwtUtil.isTokenValid(token, userDetailsService.loadUserByUsername(email))) {
            throw new RuntimeException("Invalid or expired token");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
