package com.freelance.freelancepm.repository;

import com.freelance.freelancepm.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Integer>, JpaSpecificationExecutor<Project> {
    // Required to find only the projects belonging to the logged-in manager
    Optional<Project> findByIdAndManagerId(Integer id, Integer managerId);
}