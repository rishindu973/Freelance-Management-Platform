package com.freelance.freelancepm.service;

import com.freelance.freelancepm.entity.User;
import com.freelance.freelancepm.exception.ResourceNotFoundException;
import com.freelance.freelancepm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final IEmailService emailService;

    @Override
    @Transactional
    public void verifyUser(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired verification token"));

        user.setEnabled(true);
        user.setVerificationToken(null);
        userRepository.save(user);
    }
}
