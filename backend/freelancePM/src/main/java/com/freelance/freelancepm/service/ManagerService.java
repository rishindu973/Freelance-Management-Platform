package com.freelance.freelancepm.service;

import com.freelance.freelancepm.dto.ManagerDTO;
import com.freelance.freelancepm.entity.Manager;
import com.freelance.freelancepm.repository.ManagerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.freelance.freelancepm.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class ManagerService {

    private final ManagerRepository managerRepository;

    public ManagerDTO getManagerProfile(Integer managerId) {
        Manager manager = managerRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id: " + managerId));

        ManagerDTO dto = new ManagerDTO();
        dto.setId(manager.getId());
        dto.setFullName(manager.getFullName());
        dto.setCompanyName(manager.getCompanyName());
        dto.setContactNumber(manager.getContactNumber());
        if (manager.getUser() != null) {
            dto.setEmail(manager.getUser().getEmail());
        }
        return dto;
    }
}
