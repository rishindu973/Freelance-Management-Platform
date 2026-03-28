package com.freelance.freelancepm.repository;

import com.freelance.freelancepm.entity.Freelancer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FreelancerRepository extends JpaRepository<Freelancer, Integer> {
    Boolean existsByFullName(String fullName);

    Optional<Freelancer> findByFullName(String fullName);

    Optional<Freelancer> findByUserEmail(String email);
}
