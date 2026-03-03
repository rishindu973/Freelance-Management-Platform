package com.freelance.freelancepm.controller;

import com.freelance.freelancepm.dto.FreelancerDTO;
import com.freelance.freelancepm.entity.Freelancer;
import com.freelance.freelancepm.service.FreelancerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/freelancer")
@CrossOrigin(origins = "http://localhost:5173")
public class FreelancerController {
  private final FreelancerService freelancerService;

  @Autowired
  public FreelancerController(FreelancerService freelancerService) {
    this.freelancerService = freelancerService;
  }

  @PostMapping("/create")
  public ResponseEntity<Object> create(@RequestBody FreelancerDTO freelancerDTO) {
    Object createdFreelancer = freelancerService.createFreelancer(freelancerDTO);
    return new ResponseEntity<>(createdFreelancer, HttpStatus.CREATED);
  }

  @GetMapping
  public ResponseEntity<List<Freelancer>> getAllFreelancers() {
    return new ResponseEntity<>(freelancerService.getAllFreelancers(), HttpStatus.OK);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Freelancer> getFreelancerById(@PathVariable Integer id) {
    Freelancer freelancer = freelancerService.getFreelancerById(id);
    return new ResponseEntity<>(freelancer, HttpStatus.OK);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Freelancer> updateFreelancer(@PathVariable Integer id,
      @RequestBody FreelancerDTO freelancerDTO) {
    Freelancer freelancer = freelancerService.updateFreelancer(id, freelancerDTO);
    return new ResponseEntity<>(freelancer, HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Freelancer> deleteFreelancerById(@PathVariable Integer id) {
    freelancerService.deleteFreelancer(id);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
