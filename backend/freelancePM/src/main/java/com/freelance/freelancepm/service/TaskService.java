package com.freelance.freelancepm.service;

import com.freelance.freelancepm.dto.TaskDTO;
import com.freelance.freelancepm.dto.TaskRequest;
import com.freelance.freelancepm.entity.Project;
import com.freelance.freelancepm.entity.Task;
import com.freelance.freelancepm.entity.User;
import com.freelance.freelancepm.repository.ProjectRepository;
import com.freelance.freelancepm.repository.TaskRepository;
import com.freelance.freelancepm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final com.freelance.freelancepm.repository.FreelancerRepository freelancerRepository;
    private final EmailService emailService;

    @Transactional
    public TaskDTO assignTask(TaskRequest request, Integer managerId) {
        User freelancerUser = userRepository.findById(request.getFreelancerId())
                .orElseThrow(() -> new RuntimeException("Freelancer not found by ID"));

        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setFreelancer(freelancerUser);
        task.setManager(manager);
        task.setProject(project);
        task.setDeadline(request.getDeadline());
        task.setPriority(request.getPriority());
        task.setStatus("todo");
        
        Task savedTask = taskRepository.save(task);

        // Optionally send email
        try {
            com.freelance.freelancepm.entity.Freelancer f = freelancerRepository.findById(freelancerUser.getId()).orElse(null);
            String fullName = f != null ? f.getFullName() : "Freelancer";
            
            emailService.sendTaskAssignmentEmail(
                    freelancerUser.getEmail(),
                    fullName,
                    request.getTitle());
        } catch (Exception e) {
            System.err.println("Failed to send task assignment email: " + e.getMessage());
        }

        return mapToDTO(savedTask);
    }

    public List<TaskDTO> getTasksByProjectId(Integer projectId) {
        return taskRepository.findByProjectId(projectId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public TaskDTO updateTaskStatus(Integer taskId, String status) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setStatus(status);
        Task savedTask = taskRepository.save(task);
        return mapToDTO(savedTask);
    }

    private TaskDTO mapToDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        if (task.getFreelancer() != null) {
            dto.setFreelancerEmail(task.getFreelancer().getEmail());
            com.freelance.freelancepm.entity.Freelancer f = freelancerRepository.findById(task.getFreelancer().getId()).orElse(null);
            dto.setFreelancerName(f != null ? f.getFullName() : "Freelancer");
        }
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        dto.setDeadline(task.getDeadline());
        if (task.getProject() != null) {
            dto.setProjectId(task.getProject().getId());
        }
        return dto;
    }
}
