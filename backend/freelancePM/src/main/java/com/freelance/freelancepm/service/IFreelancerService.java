package com.freelance.freelancepm.service;

import com.freelance.freelancepm.dto.FreelancerDTO;
import com.freelance.freelancepm.dto.TeamResponseDTO;
import com.freelance.freelancepm.entity.Freelancer;

import java.util.List;

public interface IFreelancerService {
    TeamResponseDTO createFreelancer(FreelancerDTO freelancerDTO, Integer managerId);

    List<Freelancer> getAllFreelancers();

    Freelancer getFreelancerById(Integer user_id);

    Freelancer updateFreelancer(Integer user_id, FreelancerDTO freelancerDTO);

    void deleteFreelancer(Integer user_id);
}
