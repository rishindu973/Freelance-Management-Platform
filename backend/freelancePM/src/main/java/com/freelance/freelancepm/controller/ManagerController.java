package com.freelance.freelancepm.controller;

import com.freelance.freelancepm.dto.ManagerDTO;
import com.freelance.freelancepm.service.ManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/manager")
@CrossOrigin(origins = "http://localhost:5173")
public class ManagerController {

    private final ManagerService managerService;

    @Autowired
    public ManagerController(ManagerService managerService) {
        this.managerService = managerService;
    }

    @GetMapping("/profile")
    public ResponseEntity<ManagerDTO> getManagerProfile() {
        // Hardcoding to ID 1 as requested for the MVP authenticated user simulation
        ManagerDTO profile = managerService.getManagerProfile(1);
        return new ResponseEntity<>(profile, HttpStatus.OK);
    }
}
