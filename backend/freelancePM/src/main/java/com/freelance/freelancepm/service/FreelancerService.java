package com.freelance.freelancepm.service;

import com.freelance.freelancepm.dto.FreelancerDTO;
import com.freelance.freelancepm.dto.TeamResponseDTO;
import com.freelance.freelancepm.dto.freelancerUpdateDTO;
import com.freelance.freelancepm.entity.Freelancer;
import com.freelance.freelancepm.entity.User;
import com.freelance.freelancepm.exception.ResourceNotFoundException;
import com.freelance.freelancepm.repository.FreelancerRepository;
import com.freelance.freelancepm.repository.ManagerRepository;
import com.freelance.freelancepm.repository.UserRepository;
import com.freelance.freelancepm.util.PasswordGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FreelancerService {
    private final FreelancerRepository freelancerRepository;
    private final UserRepository userRepository;
    private final ManagerRepository managerRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Object createFreelancer(FreelancerDTO freelancerDTO){
        if(freelancerRepository.existsByFullName(freelancerDTO.getFullName())){
            throw new IllegalArgumentException("Freelancer already exists");
        }
        String rawPassword=PasswordGenerator.generatePassword(12);
        String encodedPassword=passwordEncoder.encode(rawPassword);
        User systemManager = userRepository.findById(1).orElseThrow(() -> new RuntimeException("Manager not found"));
        User newUser=new User();
        newUser.setEmail(freelancerDTO.getEmail());
        newUser.setPassword(encodedPassword);
        newUser.setRole("Freelancer");

        Freelancer freelancer=new Freelancer();
        freelancer.setFullName(freelancerDTO.getFullName());
        freelancer.setContactNumber(freelancerDTO.getContactNumber());
        freelancer.setSalary(freelancerDTO.getSalary());
        freelancer.setTitle(freelancerDTO.getTitle());
        freelancer.setStatus(freelancerDTO.getStatus());
        freelancer.setDriveLink(freelancerDTO.getDriveLink());
        freelancer.setManager(systemManager);
        freelancer.setUser(newUser);
        freelancerRepository.save(freelancer);

        TeamResponseDTO response=new TeamResponseDTO();
        response.setMemberName(freelancerDTO.getFullName());
        response.setEmail(newUser.getEmail());
        response.setPassword(rawPassword);
        return response;
    }
    public List<Freelancer> getAllFreelancers(){
        return freelancerRepository.findAll();
    }
    public Freelancer getFreelancerById(Integer user_id){
        return freelancerRepository.findById(user_id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + user_id));
    }
    public Freelancer updateFreelancer(Integer user_id,FreelancerDTO freelancerDTO){
        Freelancer FreelancerToUpdate=getFreelancerById(user_id);
        Optional<Freelancer> freelancerWithNewUsername = freelancerRepository.findByFullName(freelancerDTO.getFullName());
        if(freelancerWithNewUsername.isPresent() && !freelancerWithNewUsername.get().getId().equals(user_id)){
            throw new IllegalArgumentException("Freelancer already exists");
        }

        FreelancerToUpdate.setFullName(freelancerDTO.getFullName());
        if(freelancerDTO.getContactNumber()!=null && !freelancerDTO.getContactNumber().isEmpty()){
            FreelancerToUpdate.setContactNumber(freelancerDTO.getContactNumber());
        }
        if(freelancerDTO.getDriveLink()!=null && !freelancerDTO.getDriveLink().isEmpty()){
            FreelancerToUpdate.setDriveLink(freelancerDTO.getDriveLink());
        }
        if(freelancerDTO.getTitle()!=null && !freelancerDTO.getTitle().isEmpty()){
            FreelancerToUpdate.setTitle(freelancerDTO.getTitle());
        }

        return freelancerRepository.save(FreelancerToUpdate);
    }
    public void deleteFreelancer(Integer user_id){
        if(!userRepository.existsById(user_id)){
            throw new IllegalArgumentException("Freelancer not found with id: " + user_id);
        }
        userRepository.deleteById(user_id);
    }


}
