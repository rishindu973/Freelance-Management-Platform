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

    @GetMapping("/assignments/{projectId}")
    public ResponseEntity<?> getAssignmentById(@PathVariable Integer projectId, Principal principal) {
        Freelancer freelancer = getAuthenticatedFreelancer(principal);
        List<ProjectResponse> assignedProjects = projectService.getProjectsForFreelancer(freelancer.getId());
        
        ProjectResponse found = assignedProjects.stream()
                .filter(p -> p.getId().equals(projectId))
                .findFirst()
                .orElse(null);

        if (found == null) {
            return ResponseEntity.status(403)
                    .body(java.util.Map.of("error", "Access denied. This project is not assigned to you."));
        }
        return ResponseEntity.ok(found);
    }

    @PatchMapping("/availability")
    public ResponseEntity<?> updateAvailability(@RequestBody java.util.Map<String, String> body, Principal principal) {
        Freelancer freelancer = getAuthenticatedFreelancer(principal);
        String newStatus = body.get("status");

        if (newStatus == null || (!newStatus.equalsIgnoreCase("available") && !newStatus.equalsIgnoreCase("busy") && !newStatus.equalsIgnoreCase("unavailable"))) {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", "Status must be 'available', 'busy', or 'unavailable'."));
        }

        freelancer.setStatus(newStatus.toLowerCase());
        freelancerRepository.save(freelancer);

        return ResponseEntity.ok(java.util.Map.of(
                "message", "Availability updated successfully.",
                "status", freelancer.getStatus()
        ));
    }
}
