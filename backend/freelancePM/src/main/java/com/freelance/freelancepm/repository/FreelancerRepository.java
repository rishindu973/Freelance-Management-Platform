package com.freelance.freelancepm.repository;

import com.freelance.freelancepm.entity.Freelancer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

@Repository
public interface FreelancerRepository extends JpaRepository<Freelancer, Integer> {
    Page<Freelancer> findAllByManagerId(Integer managerId, Pageable pageable);

    Optional<Freelancer> findByIdAndManagerId(Integer id, Integer managerId);

    Boolean existsByFullName(String fullName);

    Optional<Freelancer> findByFullName(String fullName);

    Optional<Freelancer> findByUserEmail(String email);
}
