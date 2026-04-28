package com.freelance.freelancepm.service;

import com.freelance.freelancepm.dto.ManagerDTO;
import com.freelance.freelancepm.entity.Manager;
import com.freelance.freelancepm.entity.User;
import com.freelance.freelancepm.exception.ResourceNotFoundException;
import com.freelance.freelancepm.repository.ManagerRepository;
import com.freelance.freelancepm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ManagerService implements IManagerService {

    private final ManagerRepository managerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final IEmailService emailService;

    @Override
    @Transactional
    public Manager registerNewManager(ManagerDTO managerDTO) {
        if (userRepository.existsByEmail(managerDTO.getEmail())) {
            throw new RuntimeException("Email is already registered");
        }
        String token = UUID.randomUUID().toString();

        // Generate temporary password
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);

        // Create User entity
        User user = new User();
        user.setEmail(managerDTO.getEmail());
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setRole("MANAGER");
        user.setVerificationToken(token);
        user = userRepository.saveAndFlush(user);

        // Create Manager profile linked to User
        Manager manager = new Manager();
        manager.setUser(user);
        manager.setFullName(managerDTO.getFullName());
        manager.setCompanyName(managerDTO.getCompanyName());
        manager.setContactNumber(managerDTO.getContactNumber());
        manager = managerRepository.save(manager);

        // Log the credentials to terminal for easy access during development
        System.out.println("\n==============================================");
        System.out.println("MANAGER SUCCESSFULLY REGISTERED");
        System.out.println("Email: " + user.getEmail());
        System.out.println("Password: " + tempPassword);
        System.out.println("==============================================\n");

        // Send credentials via email
        emailService.sendWelcomeEmail(user.getEmail(), tempPassword);
        emailService.sendVerificationEmail(user.getEmail(), token);

        return manager;
    }

    @Override
    public ManagerDTO getManagerProfile(String email) {
        Manager manager = managerRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Manager profile not found for email: " + email));

        ManagerDTO dto = new ManagerDTO();
        dto.setId(manager.getId());
        dto.setEmail(manager.getUser().getEmail());
        dto.setFullName(manager.getFullName());
        dto.setCompanyName(manager.getCompanyName());
        dto.setContactNumber(manager.getContactNumber());
        return dto;
    }

    // Keep the old integer based profile fetch for compatibility if needed.
    public ManagerDTO getManagerProfile(Integer managerId) {
        Manager manager = managerRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id: " + managerId));

        ManagerDTO dto = new ManagerDTO();
        dto.setId(manager.getId());
        dto.setFullName(manager.getFullName());
        dto.setCompanyName(manager.getCompanyName());
        dto.setContactNumber(manager.getContactNumber());
        if (manager.getUser() != null) {
            dto.setEmail(manager.getUser().getEmail());
        }
        return dto;
    }

    @Override
    public Integer getManagerIdByEmail(String email) {
        return managerRepository.findByUserEmail(email)
                .map(Manager::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found for email: " + email));
    }
}
