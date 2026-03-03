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

    @Query(value = "SELECT COUNT(*) FROM project p WHERE p.manager_id = :managerId", nativeQuery = true)
    long countAllByManager(Integer managerId);

    @Query(value = """
      SELECT COUNT(*) FROM project p
      WHERE p.manager_id = :managerId
        AND LOWER(p.status) = 'completed'
      """, nativeQuery = true)
    long countCompletedByManager(Integer managerId);

    @Query(value = """
      SELECT COUNT(*) FROM project p
      WHERE p.manager_id = :managerId
        AND LOWER(p.status) = 'pending'
      """, nativeQuery = true)
    long countPendingByManager(Integer managerId);

    @Query(value = """
      SELECT COUNT(*) FROM project p
      WHERE p.manager_id = :managerId
        AND LOWER(p.status) NOT IN ('pending','completed')
      """, nativeQuery = true)
    long countActiveByManager(Integer managerId);

    @Query(value = """
      SELECT COALESCE(LOWER(p.status), 'unknown') AS status, COUNT(*) AS total
      FROM project p
      WHERE p.manager_id = :managerId
      GROUP BY COALESCE(LOWER(p.status), 'unknown')
      ORDER BY total DESC
      """, nativeQuery = true)
    List<Object[]> statusBreakdown(Integer managerId);

    @Query(value = """
      SELECT * FROM project p
      WHERE p.manager_id = :managerId
        AND p.deadline IS NOT NULL
        AND p.deadline >= :today
        AND LOWER(p.status) <> 'completed'
      ORDER BY p.deadline ASC
      LIMIT :limit
      """, nativeQuery = true)
    List<Project> upcomingDeadlines(Integer managerId, LocalDate today, int limit);

    @Query(value = """
      SELECT * FROM project p
      WHERE p.manager_id = :managerId
        AND LOWER(p.status) = 'completed'
      ORDER BY p.id DESC
      LIMIT :limit
      """, nativeQuery = true)
    List<Project> recentCompleted(Integer managerId, int limit);

    @Query(value = """
      SELECT * FROM project p
      WHERE p.manager_id = :managerId
        AND LOWER(p.status) = 'pending'
      ORDER BY p.id DESC
      LIMIT :limit
      """, nativeQuery = true)
    List<Project> pendingProjects(Integer managerId, int limit);
}