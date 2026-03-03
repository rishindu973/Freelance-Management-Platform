package com.freelance.freelancepm.repository;

import com.freelance.freelancepm.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Interface Segregation- Only exposes CRUD operations for Client
@Repository
public interface ClientRepository extends JpaRepository<Client, Integer> {

}