package com.freelance.freelancepm.controller;

import com.freelance.freelancepm.dto.ProjectResponse;
import com.freelance.freelancepm.entity.Freelancer;
import com.freelance.freelancepm.repository.FreelancerRepository;
import com.freelance.freelancepm.repository.ProjectRepository;
import com.freelance.freelancepm.dto.TeamMemberDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/freelancer")
@RequiredArgsConstructor
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:5174" })
public class FreelancerPortalController {

    private final FreelancerRepository freelancerRepository;
    private final ProjectRepository projectRepository;

    /**
     * GET /api/freelancer/profile
     * Returns the authenticated freelancer's own profile.
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Freelancer freelancer = freelancerRepository.findByUserEmail(principal.getName())
                .orElse(null);

        if (freelancer == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied. Not a freelancer account."));
        }

        return ResponseEntity.ok(Map.of(
                "id", freelancer.getId(),
                "fullName", freelancer.getFullName(),
                "title", freelancer.getTitle() != null ? freelancer.getTitle() : "",
                "contactNumber", freelancer.getContactNumber() != null ? freelancer.getContactNumber() : "",
                "status", freelancer.getStatus() != null ? freelancer.getStatus() : "active",
                "email", principal.getName()
        ));
    }

    /**
     * GET /api/freelancer/assignments
     * Returns all projects assigned to the authenticated freelancer.
     * Filters by User_ID from JWT (RBAC enforced).
     */
    @GetMapping("/assignments")
    public ResponseEntity<?> getAssignments(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Freelancer freelancer = freelancerRepository.findByUserEmail(principal.getName())
                .orElse(null);

        if (freelancer == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied. Not a freelancer account."));
        }

        List<ProjectResponse> projects = projectRepository
                .findAllByFreelancerId(freelancer.getId())
                .stream()
                .map(p -> {
                    List<TeamMemberDTO> teamDto = p.getTeam() != null
                            ? p.getTeam().stream()
                                    .map(f -> TeamMemberDTO.builder()
                                            .id(f.getId())
                                            .name(f.getFullName())
                                            .role(f.getTitle())
                                            .initials(getInitials(f.getFullName()))
                                            .build())
                                    .toList()
                            : new java.util.ArrayList<>();

                    return ProjectResponse.builder()
                            .id(p.getId())
                            .clientId(p.getClientId())
                            .managerId(p.getManagerId())
                            .name(p.getName())
                            .description(p.getDescription())
                            .type(p.getType())
                            .startDate(p.getStartDate())
                            .deadline(p.getDeadline())
                            .status(p.getStatus())
                            .team(teamDto)
                            .build();
                })
                .toList();

        return ResponseEntity.ok(projects);
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) return "??";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}
