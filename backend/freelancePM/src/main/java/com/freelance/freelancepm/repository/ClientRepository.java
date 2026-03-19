package com.freelance.freelancepm.repository;

import com.freelance.freelancepm.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Integer> {
}