package com.freelance.freelancepm.service;

import com.freelance.freelancepm.dto.FreelancerDTO;
import com.freelance.freelancepm.dto.TeamResponseDTO;
import com.freelance.freelancepm.entity.Freelancer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IFreelancerService {
    TeamResponseDTO createFreelancer(FreelancerDTO freelancerDTO, Integer managerId);

    Page<FreelancerDTO> getAllFreelancers(Integer managerId, Pageable pageable);

    Freelancer getFreelancerById(Integer user_id, Integer managerId);

    FreelancerDTO updateFreelancer(Integer user_id, FreelancerDTO freelancerDTO, Integer managerId);

    void deleteFreelancer(Integer user_id, Integer managerId);
}
