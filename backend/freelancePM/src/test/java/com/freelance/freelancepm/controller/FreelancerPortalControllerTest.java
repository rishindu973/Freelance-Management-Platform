package com.freelance.freelancepm.controller;

import com.freelance.freelancepm.dto.ProjectResponse;
import com.freelance.freelancepm.entity.Freelancer;
import com.freelance.freelancepm.repository.FreelancerRepository;
import com.freelance.freelancepm.service.IProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class FreelancerPortalControllerTest {

    @Mock
    private FreelancerRepository freelancerRepository;

    @Mock
    private IProjectService projectService;

    @Mock
    private Principal principal;

    @InjectMocks
    private FreelancerPortalController controller;

    private Freelancer testFreelancer;
    private ProjectResponse testProject;

    @BeforeEach
    void setUp() {
        testFreelancer = new Freelancer();
        testFreelancer.setId(1);
        testFreelancer.setFullName("John Doe");
        testFreelancer.setStatus("available");

        testProject = ProjectResponse.builder()
                .id(100)
                .name("Test Project")
                .clientName("Test Client")
                .status("in progress")
                .build();
    }

    @Test
    void getProfile_ReturnsFreelancer() {
        when(principal.getName()).thenReturn("test@test.com");
        when(freelancerRepository.findByUserEmail("test@test.com")).thenReturn(Optional.of(testFreelancer));

        ResponseEntity<Freelancer> response = controller.getProfile(principal);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(testFreelancer.getFullName(), response.getBody().getFullName());
    }

    @Test
    void getAssignments_ReturnsProjects() {
        when(principal.getName()).thenReturn("test@test.com");
        when(freelancerRepository.findByUserEmail("test@test.com")).thenReturn(Optional.of(testFreelancer));
        when(projectService.getProjectsForFreelancer(1)).thenReturn(List.of(testProject));

        ResponseEntity<List<ProjectResponse>> response = controller.getAssignments(principal);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals(testProject.getId(), response.getBody().get(0).getId());
    }

    @Test
    void getAssignmentById_ReturnsProject_WhenHasAccess() {
        when(principal.getName()).thenReturn("test@test.com");
        when(freelancerRepository.findByUserEmail("test@test.com")).thenReturn(Optional.of(testFreelancer));
        when(projectService.getProjectsForFreelancer(1)).thenReturn(List.of(testProject));

        ResponseEntity<?> response = controller.getAssignmentById(100, principal);

        assertEquals(200, response.getStatusCode().value());
        ProjectResponse p = (ProjectResponse) response.getBody();
        assertEquals(100, p.getId());
    }

    @Test
    void getAssignmentById_Returns403_WhenNoAccess() {
        when(principal.getName()).thenReturn("test@test.com");
        when(freelancerRepository.findByUserEmail("test@test.com")).thenReturn(Optional.of(testFreelancer));
        when(projectService.getProjectsForFreelancer(1)).thenReturn(List.of(testProject)); // 100 assigned

        ResponseEntity<?> response = controller.getAssignmentById(999, principal);

        assertEquals(403, response.getStatusCode().value());
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Access denied. This project is not assigned to you.", body.get("error"));
    }

    @Test
    void updateAvailability_ChangesStatus() {
        when(principal.getName()).thenReturn("test@test.com");
        when(freelancerRepository.findByUserEmail("test@test.com")).thenReturn(Optional.of(testFreelancer));

        ResponseEntity<?> response = controller.updateAvailability(Map.of("status", "unavailable"), principal);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("unavailable", testFreelancer.getStatus());
        verify(freelancerRepository).save(testFreelancer);
    }

    @Test
    void updateAvailability_InvalidStatus_Returns400() {
        when(principal.getName()).thenReturn("test@test.com");
        when(freelancerRepository.findByUserEmail("test@test.com")).thenReturn(Optional.of(testFreelancer));

        ResponseEntity<?> response = controller.updateAvailability(Map.of("status", "invalid"), principal);

        assertEquals(400, response.getStatusCode().value());
    }
}
