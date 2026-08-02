package com.freelance.freelancepm.service;

import com.freelance.freelancepm.dto.FreelancerDTO;
import com.freelance.freelancepm.dto.TeamResponseDTO;
import com.freelance.freelancepm.entity.Freelancer;
import com.freelance.freelancepm.entity.User;
import com.freelance.freelancepm.exception.ResourceNotFoundException;
import com.freelance.freelancepm.repository.FreelancerRepository;
import com.freelance.freelancepm.repository.ManagerRepository;
import com.freelance.freelancepm.repository.UserRepository;
import com.freelance.freelancepm.entity.Manager;
import com.freelance.freelancepm.util.PasswordGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FreelancerService implements IFreelancerService {
    private final FreelancerRepository freelancerRepository;
    private final UserRepository userRepository;
    private final ManagerRepository managerRepository;
    private final PasswordEncoder passwordEncoder;
    private final IEmailService emailService;
    private final ActivityService activityService;

    @Transactional
    @Override
    public TeamResponseDTO createFreelancer(FreelancerDTO freelancerDTO, Integer managerId) {
        if (freelancerRepository.findByFullName(freelancerDTO.getFullName()).filter(f -> f.getManager().getId().equals(managerId)).isPresent()) {
            throw new IllegalArgumentException("Freelancer already exists");
        }
        String rawPassword = PasswordGenerator.generatePassword(12);
        String encodedPassword = passwordEncoder.encode(rawPassword);
        String token = java.util.UUID.randomUUID().toString();

        // Fetch the Manager entity, then get the underlying User for the foreign key
        Manager managerEntity = managerRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found with id: " + managerId));
        User managerUser = managerEntity.getUser();

        User newUser = new User();
        newUser.setEmail(freelancerDTO.getEmail());
        newUser.setPassword(encodedPassword);
        newUser.setRole("FREELANCER");
        newUser.setVerificationToken(token);
        userRepository.save(newUser);

        Freelancer freelancer = new Freelancer();
        freelancer.setFullName(freelancerDTO.getFullName());
        freelancer.setContactNumber(freelancerDTO.getContactNumber());
        freelancer.setSalary(freelancerDTO.getSalary());
        freelancer.setTitle(freelancerDTO.getTitle());
        freelancer.setStatus(freelancerDTO.getStatus());
        freelancer.setDriveLink(freelancerDTO.getDriveLink());
        freelancer.setManager(managerUser);
        freelancer.setUser(newUser);
        freelancerRepository.save(freelancer);

        // Log the credentials to terminal for easy access during development
        System.out.println("\n==============================================");
        System.out.println("FREELANCER SUCCESSFULLY CREATED");
        System.out.println("Email: " + newUser.getEmail());
        System.out.println("Password: " + rawPassword);
        System.out.println("==============================================\n");

        // SRP: Delegate credential delivery to EmailService
        emailService.sendWelcomeEmail(newUser.getEmail(), rawPassword);
        emailService.sendVerificationEmail(newUser.getEmail(), token);

        activityService.logActivity(managerId, com.freelance.freelancepm.entity.Activity.ActivityType.MEMBER_ADDED, "Added new freelancer: " + freelancerDTO.getFullName());

        TeamResponseDTO response = new TeamResponseDTO();
        response.setMemberName(freelancerDTO.getFullName());
        response.setEmail(newUser.getEmail());
        response.setPassword(rawPassword);
        return response;
    }

    @Override
    public Page<FreelancerDTO> getAllFreelancers(Integer managerId, org.springframework.data.domain.Pageable pageable) {
        return freelancerRepository.findAllByManagerId(managerId, pageable)
                .map(this::mapToDTO);
    }
    
    private FreelancerDTO mapToDTO(Freelancer freelancer) {
        FreelancerDTO dto = new FreelancerDTO();
        dto.setId(freelancer.getId());
        dto.setFullName(freelancer.getFullName());
        dto.setTitle(freelancer.getTitle());
        dto.setContactNumber(freelancer.getContactNumber());
        dto.setSalary(freelancer.getSalary());
        dto.setStatus(freelancer.getStatus());
        dto.setDriveLink(freelancer.getDriveLink());
        if (freelancer.getUser() != null) {
            dto.setEmail(freelancer.getUser().getEmail());
            dto.setRole(freelancer.getUser().getRole());
        }
        return dto;
    }

    @Override
    public Freelancer getFreelancerById(Integer user_id, Integer managerId) {
        return freelancerRepository.findByIdAndManagerId(user_id, managerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + user_id));
    }

    @Override
    public FreelancerDTO updateFreelancer(Integer user_id, FreelancerDTO freelancerDTO, Integer managerId) {
        Freelancer FreelancerToUpdate = getFreelancerById(user_id, managerId);
        Optional<Freelancer> freelancerWithNewUsername = freelancerRepository
                .findByFullName(freelancerDTO.getFullName())
                .filter(f -> f.getManager().getId().equals(managerId));
        if (freelancerWithNewUsername.isPresent() && !freelancerWithNewUsername.get().getId().equals(user_id)) {
            throw new IllegalArgumentException("Freelancer already exists");
        }

        FreelancerToUpdate.setFullName(freelancerDTO.getFullName());
        if (freelancerDTO.getContactNumber() != null && !freelancerDTO.getContactNumber().isEmpty()) {
            FreelancerToUpdate.setContactNumber(freelancerDTO.getContactNumber());
        }
        if (freelancerDTO.getDriveLink() != null && !freelancerDTO.getDriveLink().isEmpty()) {
            FreelancerToUpdate.setDriveLink(freelancerDTO.getDriveLink());
        }
        if (freelancerDTO.getTitle() != null && !freelancerDTO.getTitle().isEmpty()) {
            FreelancerToUpdate.setTitle(freelancerDTO.getTitle());
        }

        Freelancer saved = freelancerRepository.save(FreelancerToUpdate);
        return mapToDTO(saved);
    }

    @Override
    public void deleteFreelancer(Integer user_id, Integer managerId) {
        Freelancer freelancer = getFreelancerById(user_id, managerId);
        freelancerRepository.delete(freelancer);
        userRepository.deleteById(freelancer.getId());
    }

}
