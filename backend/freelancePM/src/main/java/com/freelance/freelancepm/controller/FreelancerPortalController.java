package com.freelance.freelancepm.controller;

import com.freelance.freelancepm.dto.ProjectResponse;
import com.freelance.freelancepm.entity.Freelancer;
import com.freelance.freelancepm.repository.FreelancerRepository;
import com.freelance.freelancepm.service.IProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/freelancer")
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:5174" })
@RequiredArgsConstructor
public class FreelancerPortalController {

    private final FreelancerRepository freelancerRepository;
    private final IProjectService projectService;

    private Freelancer getAuthenticatedFreelancer(Principal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("Not authenticated");
        }
        return freelancerRepository.findByUserEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Freelancer profile not found for email"));
    }

    @GetMapping("/profile")
    public ResponseEntity<Freelancer> getProfile(Principal principal) {
        return ResponseEntity.ok(getAuthenticatedFreelancer(principal));
    }

    @GetMapping("/assignments")
    public ResponseEntity<List<ProjectResponse>> getAssignments(Principal principal) {
        Freelancer freelancer = getAuthenticatedFreelancer(principal);
        List<ProjectResponse> projects = projectService.getProjectsForFreelancer(freelancer.getId());
        return ResponseEntity.ok(projects);
    }
}
