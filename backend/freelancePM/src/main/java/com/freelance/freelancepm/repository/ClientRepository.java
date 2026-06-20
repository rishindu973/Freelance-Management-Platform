package com.freelance.freelancepm.repository;

import com.freelance.freelancepm.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

// Interface Segregation: Only exposes CRUD operations for Client
@Repository
public interface ClientRepository extends JpaRepository<Client, Integer> {
    Page<Client> findAllByManagerId(Integer managerId, Pageable pageable);
    Optional<Client> findByIdAndManagerId(Integer id, Integer managerId);
}