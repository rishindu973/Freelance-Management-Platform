package com.freelance.freelancepm.controller;

import com.freelance.freelancepm.dto.ManagerDTO;
import com.freelance.freelancepm.service.ManagerService;
import com.freelance.freelancepm.entity.Manager;
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

    @PostMapping("/register")
    public ResponseEntity<ManagerDTO> register(@RequestBody ManagerDTO managerDTO) {
        Manager manager = managerService.registerNewManager(managerDTO);
        managerDTO.setId(manager.getId());
        return new ResponseEntity<>(managerDTO, HttpStatus.CREATED);
    }

    @GetMapping("/profile")
    public ResponseEntity<ManagerDTO> getManagerProfile(java.security.Principal principal) {
        if (principal != null) {
            return new ResponseEntity<>(managerService.getManagerProfile(principal.getName()), HttpStatus.OK);
        }
        // Fallback for testing without JWT
        ManagerDTO profile = managerService.getManagerProfile(1);
        return new ResponseEntity<>(profile, HttpStatus.OK);
    }
}
