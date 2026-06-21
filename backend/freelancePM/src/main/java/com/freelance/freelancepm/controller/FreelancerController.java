package com.freelance.freelancepm.controller;

import com.freelance.freelancepm.dto.FreelancerDTO;
import com.freelance.freelancepm.entity.Freelancer;
import com.freelance.freelancepm.service.IFreelancerService;
import com.freelance.freelancepm.service.IManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.security.Principal;

import java.util.List;

@RestController
@RequestMapping("/api/freelancers")
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:5174" })
public class FreelancerController {
  private final IFreelancerService freelancerService;
  private final IManagerService managerService;

  @Autowired
  public FreelancerController(IFreelancerService freelancerService, IManagerService managerService) {
    this.freelancerService = freelancerService;
    this.managerService = managerService;
  }

  private Integer requireManagerId(Principal principal) {
    if (principal == null) {
      throw new IllegalArgumentException("Not authenticated");
    }
    return managerService.getManagerIdByEmail(principal.getName());
  }

  @PostMapping("/create")
  public ResponseEntity<Object> create(@RequestBody FreelancerDTO freelancerDTO, Principal principal) {
    Integer managerId = requireManagerId(principal);
    Object createdFreelancer = freelancerService.createFreelancer(freelancerDTO, managerId);
    return new ResponseEntity<>(createdFreelancer, HttpStatus.CREATED);
  }

  @GetMapping
  public ResponseEntity<Page<FreelancerDTO>> getAllFreelancers(
      Principal principal,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "10") int size) {
    Integer managerId = requireManagerId(principal);
    Pageable pageable = PageRequest.of(page, size);
    return new ResponseEntity<>(freelancerService.getAllFreelancers(managerId, pageable), HttpStatus.OK);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Freelancer> getFreelancerById(@PathVariable("id") Integer id, Principal principal) {
    Integer managerId = requireManagerId(principal);
    Freelancer freelancer = freelancerService.getFreelancerById(id, managerId);
    return new ResponseEntity<>(freelancer, HttpStatus.OK);
  }

  @PutMapping("/{id}")
  public ResponseEntity<FreelancerDTO> updateFreelancer(
      @PathVariable("id") Integer id,
      @RequestBody FreelancerDTO freelancerDTO,
      Principal principal) {
    Integer managerId = requireManagerId(principal);
    FreelancerDTO updated = freelancerService.updateFreelancer(id, freelancerDTO, managerId);
    return new ResponseEntity<>(updated, HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Freelancer> deleteFreelancerById(@PathVariable("id") Integer id, Principal principal) {
    Integer managerId = requireManagerId(principal);
    freelancerService.deleteFreelancer(id, managerId);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
