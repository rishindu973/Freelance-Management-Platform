package com.freelance.freelancepm.repository;

import com.freelance.freelancepm.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {

    Optional<Project> findByIdAndManagerId(Long id, Long managerId);

    @Query(value = "SELECT COUNT(*) FROM project p WHERE p.manager_id = :managerId", nativeQuery = true)
    long countAllByManager(Long managerId);

    @Query(value = """
        SELECT COUNT(*) FROM project p
        WHERE p.manager_id = :managerId
          AND LOWER(p.status) = 'completed'
        """, nativeQuery = true)
    long countCompletedByManager(Long managerId);

    @Query(value = """
        SELECT COUNT(*) FROM project p
        WHERE p.manager_id = :managerId
          AND LOWER(p.status) = 'pending'
        """, nativeQuery = true)
    long countPendingByManager(Long managerId);

    @Query(value = """
        SELECT COUNT(*) FROM project p
        WHERE p.manager_id = :managerId
          AND LOWER(p.status) NOT IN ('pending','completed')
        """, nativeQuery = true)
    long countActiveByManager(Long managerId);

    @Query(value = """
        SELECT COALESCE(LOWER(p.status), 'unknown') AS status, COUNT(*) AS total
        FROM project p
        WHERE p.manager_id = :managerId
        GROUP BY COALESCE(LOWER(p.status), 'unknown')
        ORDER BY total DESC
        """, nativeQuery = true)
    List<Object[]> statusBreakdown(Long managerId);

    @Query(value = """
        SELECT * FROM project p
        WHERE p.manager_id = :managerId
          AND p.deadline IS NOT NULL
          AND p.deadline >= :today
          AND LOWER(p.status) <> 'completed'
        ORDER BY p.deadline ASC
        LIMIT :limit
        """, nativeQuery = true)
    List<Project> upcomingDeadlines(Long managerId, LocalDate today, int limit);

    @Query(value = """
        SELECT * FROM project p
        WHERE p.manager_id = :managerId
          AND LOWER(p.status) = 'completed'
        ORDER BY p.id DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Project> recentCompleted(Long managerId, int limit);

    @Query(value = """
        SELECT * FROM project p
        WHERE p.manager_id = :managerId
          AND LOWER(p.status) = 'pending'
        ORDER BY p.id DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Project> pendingProjects(Long managerId, int limit);

    @Query(value = """
        SELECT COUNT(*) FROM project p
        WHERE p.manager_id = :managerId
          AND p.deadline < :today
          AND LOWER(p.status) <> 'completed'
        """, nativeQuery = true)
    long countOverdueByManager(Long managerId, LocalDate today);

    @Query(value = """
        SELECT COUNT(*) FROM project p
        WHERE p.manager_id = :managerId
          AND p.deadline >= :today
          AND p.deadline <= :until
          AND LOWER(p.status) <> 'completed'
        """, nativeQuery = true)
    long countDueSoonByManager(Long managerId, LocalDate today, LocalDate until);
}