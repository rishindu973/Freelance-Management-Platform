package com.freelance.freelancepm.controller;

import com.freelance.freelancepm.dto.FreelancerDTO;
import com.freelance.freelancepm.entity.Freelancer;
import com.freelance.freelancepm.service.IFreelancerService;
import com.freelance.freelancepm.service.IManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

  @PostMapping("/create")
  public ResponseEntity<Object> create(@RequestBody FreelancerDTO freelancerDTO, java.security.Principal principal) {
    if (principal == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    Integer managerId = managerService.getManagerIdByEmail(principal.getName());
    Object createdFreelancer = freelancerService.createFreelancer(freelancerDTO, managerId);
    return new ResponseEntity<>(createdFreelancer, HttpStatus.CREATED);
  }

  @GetMapping
  public ResponseEntity<List<Freelancer>> getAllFreelancers() {
    return new ResponseEntity<>(freelancerService.getAllFreelancers(), HttpStatus.OK);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Freelancer> getFreelancerById(@PathVariable("id") Integer id) {
    Freelancer freelancer = freelancerService.getFreelancerById(id);
    return new ResponseEntity<>(freelancer, HttpStatus.OK);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Freelancer> updateFreelancer(@PathVariable("id") Integer id,
      @RequestBody FreelancerDTO freelancerDTO) {
    Freelancer freelancer = freelancerService.updateFreelancer(id, freelancerDTO);
    return new ResponseEntity<>(freelancer, HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Freelancer> deleteFreelancerById(@PathVariable("id") Integer id) {
    freelancerService.deleteFreelancer(id);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
