package com.freelance.freelancepm.service;

import com.freelance.freelancepm.entity.Task;
import com.freelance.freelancepm.entity.User;
import com.freelance.freelancepm.repository.TaskRepository;
import com.freelance.freelancepm.repository.FreelancerRepository;
import com.freelance.freelancepm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final FreelancerRepository freelancerRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Transactional
    public Task assignTask(String freelancerEmail, String title, String description, Integer managerId) {
        User freelancerUser = userRepository.findByEmail(freelancerEmail)
                .orElseThrow(() -> new RuntimeException("Freelancer not found by email"));

        com.freelance.freelancepm.entity.Freelancer freelancer = freelancerRepository.findById(freelancerUser.getId())
                .orElseThrow(() -> new RuntimeException("Freelancer details not found"));

        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setFreelancer(freelancerUser);
        task.setManager(manager);
        task.setStatus("Pending");
        Task savedTask = taskRepository.save(task);

        emailService.sendTaskAssignmentEmail(
                freelancerUser.getEmail(),
                freelancer.getFullName(),
                title);
        return savedTask;
    }

}
