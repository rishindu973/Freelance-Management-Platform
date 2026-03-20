package com.freelance.freelancepm.service;

import com.freelance.freelancepm.dto.ManagerDTO;
import com.freelance.freelancepm.entity.Manager;

public interface IManagerService {
    Manager registerNewManager(ManagerDTO managerDTO);

    ManagerDTO getManagerProfile(String email);

    Integer getManagerIdByEmail(String email);
}