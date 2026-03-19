package com.freelance.freelancepm.repository;

import com.freelance.freelancepm.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Integer>, JpaSpecificationExecutor<Project> {

    Optional<Project> findByIdAndManagerId(Integer id, Integer managerId);

    List<Project> findByClientId(Integer clientId);

    @Query("""
        SELECT p FROM Project p
        WHERE p.client.id = :clientId
          AND (:status IS NULL OR LOWER(p.status) = LOWER(:status))
          AND (:from IS NULL OR p.startDate >= :from)
          AND (:to IS NULL OR p.startDate <= :to)
        ORDER BY p.startDate ASC
    """)
    List<Project> filterProjects(Integer clientId, String status, LocalDate from, LocalDate to);

    @Query("""
        SELECT p FROM Project p
        WHERE p.managerId = :managerId
          AND p.deadline IS NOT NULL
          AND p.deadline >= :today
          AND p.status <> 'COMPLETED'
        ORDER BY p.deadline ASC
    """)
    List<Project> upcomingDeadlines(Integer managerId, LocalDate today);

    @Query("""
        SELECT p FROM Project p
        WHERE p.managerId = :managerId
          AND p.status = 'COMPLETED'
        ORDER BY p.id DESC
    """)
    List<Project> completedProjects(Integer managerId);
}