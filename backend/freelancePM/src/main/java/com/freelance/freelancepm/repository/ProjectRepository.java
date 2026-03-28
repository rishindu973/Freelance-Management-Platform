package com.freelance.freelancepm.repository;

import com.freelance.freelancepm.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Integer>, JpaSpecificationExecutor<Project> {

  // ----------------- Manager Queries -----------------
  Optional<Project> findByIdAndManagerId(Integer id, Integer managerId);

  @Query(value = "SELECT COUNT(*) FROM project p WHERE p.manager_id = :managerId", nativeQuery = true)
  long countAllByManager(@Param("managerId") Integer managerId);

  @Query(value = """
      SELECT COUNT(*) FROM project p
      WHERE p.manager_id = :managerId
        AND LOWER(p.status) = 'completed'
      """, nativeQuery = true)
  long countCompletedByManager(@Param("managerId") Integer managerId);

  @Query(value = """
      SELECT COUNT(*) FROM project p
      WHERE p.manager_id = :managerId
        AND LOWER(p.status) = 'pending'
      """, nativeQuery = true)
  long countPendingByManager(@Param("managerId") Integer managerId);

  @Query(value = """
      SELECT COUNT(*) FROM project p
      WHERE p.manager_id = :managerId
        AND LOWER(p.status) NOT IN ('pending','completed')
      """, nativeQuery = true)
  long countActiveByManager(@Param("managerId") Integer managerId);

  @Query(value = """
      SELECT COUNT(*) FROM project p
      WHERE p.manager_id = :managerId
        AND p.deadline IS NOT NULL
        AND p.deadline < :today
        AND LOWER(p.status) NOT IN ('completed')
      """, nativeQuery = true)
  long countOverdueByManager(@Param("managerId") Integer managerId, @Param("today") LocalDate today);

  @Query(value = """
      SELECT COUNT(*) FROM project p
      WHERE p.manager_id = :managerId
        AND p.deadline IS NOT NULL
        AND p.deadline >= :today
        AND p.deadline <= :until
        AND LOWER(p.status) NOT IN ('completed')
      """, nativeQuery = true)
  long countDueSoonByManager(@Param("managerId") Integer managerId, @Param("today") LocalDate today,
      @Param("until") LocalDate until);

  @Query(value = """
      SELECT COALESCE(LOWER(p.status), 'unknown') AS status, COUNT(*) AS total
      FROM project p
      WHERE p.manager_id = :managerId
      GROUP BY COALESCE(LOWER(p.status), 'unknown')
      ORDER BY total DESC
      """, nativeQuery = true)
  List<Object[]> statusBreakdown(@Param("managerId") Integer managerId);

  @Query("""
      SELECT p FROM Project p
      WHERE p.managerId = :managerId
        AND p.deadline IS NOT NULL
        AND p.deadline >= :today
        AND LOWER(p.status) <> 'completed'
      ORDER BY p.deadline ASC
      """)
  List<Project> upcomingDeadlines(@Param("managerId") Integer managerId, @Param("today") LocalDate today,
      Pageable pageable);

  @Query("""
      SELECT p FROM Project p
      WHERE p.managerId = :managerId
        AND LOWER(p.status) = 'completed'
      ORDER BY p.id DESC
      """)
  List<Project> recentCompleted(@Param("managerId") Integer managerId, Pageable pageable);

  @Query("""
      SELECT p FROM Project p
      WHERE p.managerId = :managerId
        AND LOWER(p.status) = 'pending'
      ORDER BY p.id DESC
      """)
  List<Project> pendingProjects(@Param("managerId") Integer managerId, Pageable pageable);

  // ----------------- Client Queries -----------------
  List<Project> findByClientId(Integer clientId);

  @Query("""
          SELECT p FROM Project p
          WHERE p.client.id = :clientId
            AND (:status IS NULL OR LOWER(p.status) = LOWER(:status))
            AND (:from IS NULL OR p.startDate >= :from)
            AND (:to IS NULL OR p.startDate <= :to)
          ORDER BY p.startDate ASC
      """)
  List<Project> filterProjects(@Param("clientId") Integer clientId, @Param("status") String status,
      @Param("from") LocalDate from, @Param("to") LocalDate to);

  // ----------------- Freelancer Queries -----------------
  @Query("SELECT p FROM Project p JOIN p.team f WHERE f.id = :freelancerId")
  List<Project> findAllByFreelancerId(
      @org.springframework.data.repository.query.Param("freelancerId") Integer freelancerId);
}