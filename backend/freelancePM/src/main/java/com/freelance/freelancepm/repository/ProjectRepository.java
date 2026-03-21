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
  long countAllByManager(@org.springframework.data.repository.query.Param("managerId") Integer managerId);

  @Query(value = """
      SELECT COUNT(*) FROM project p
      WHERE p.manager_id = :managerId
        AND LOWER(p.status) = 'completed'
      """, nativeQuery = true)
  long countCompletedByManager(@org.springframework.data.repository.query.Param("managerId") Integer managerId);

  @Query(value = """
      SELECT COUNT(*) FROM project p
      WHERE p.manager_id = :managerId
        AND LOWER(p.status) = 'pending'
      """, nativeQuery = true)
  long countPendingByManager(@org.springframework.data.repository.query.Param("managerId") Integer managerId);

  @Query(value = """
      SELECT COUNT(*) FROM project p
      WHERE p.manager_id = :managerId
        AND LOWER(p.status) NOT IN ('pending','completed')
      """, nativeQuery = true)
  long countActiveByManager(@org.springframework.data.repository.query.Param("managerId") Integer managerId);

  @Query(value = """
      SELECT COUNT(*) FROM project p
      WHERE p.manager_id = :managerId
        AND p.deadline IS NOT NULL
        AND p.deadline < :today
        AND LOWER(p.status) NOT IN ('completed')
      """, nativeQuery = true)
  long countOverdueByManager(@org.springframework.data.repository.query.Param("managerId") Integer managerId,
      @org.springframework.data.repository.query.Param("today") LocalDate today);

  @Query(value = """
      SELECT COUNT(*) FROM project p
      WHERE p.manager_id = :managerId
        AND p.deadline IS NOT NULL
        AND p.deadline >= :today
        AND p.deadline <= :until
        AND LOWER(p.status) NOT IN ('completed')
      """, nativeQuery = true)
  long countDueSoonByManager(@org.springframework.data.repository.query.Param("managerId") Integer managerId,
      @org.springframework.data.repository.query.Param("today") LocalDate today,
      @org.springframework.data.repository.query.Param("until") LocalDate until);

  @Query(value = """
      SELECT COALESCE(LOWER(p.status), 'unknown') AS status, COUNT(*) AS total
      FROM project p
      WHERE p.manager_id = :managerId
      GROUP BY COALESCE(LOWER(p.status), 'unknown')
      ORDER BY total DESC
      """, nativeQuery = true)
  List<Object[]> statusBreakdown(@org.springframework.data.repository.query.Param("managerId") Integer managerId);

  @Query(value = """
      SELECT * FROM project p
      WHERE p.manager_id = :managerId
        AND p.deadline IS NOT NULL
        AND p.deadline >= :today
        AND LOWER(p.status) <> 'completed'
      ORDER BY p.deadline ASC
      LIMIT :limit
      """, nativeQuery = true)
  List<Project> upcomingDeadlines(@org.springframework.data.repository.query.Param("managerId") Integer managerId,
      @org.springframework.data.repository.query.Param("today") LocalDate today,
      @org.springframework.data.repository.query.Param("limit") int limit);

  @Query(value = """
      SELECT * FROM project p
      WHERE p.manager_id = :managerId
        AND LOWER(p.status) = 'completed'
      ORDER BY p.id DESC
      LIMIT :limit
      """, nativeQuery = true)
  List<Project> recentCompleted(@org.springframework.data.repository.query.Param("managerId") Integer managerId,
      @org.springframework.data.repository.query.Param("limit") int limit);

  @Query(value = """
      SELECT * FROM project p
      WHERE p.manager_id = :managerId
        AND LOWER(p.status) = 'pending'
      ORDER BY p.id DESC
      LIMIT :limit
      """, nativeQuery = true)
  List<Project> pendingProjects(@org.springframework.data.repository.query.Param("managerId") Integer managerId,
      @org.springframework.data.repository.query.Param("limit") int limit);

  @Query(value = """
      SELECT p.* FROM project p
      INNER JOIN project_freelancer pf ON p.id = pf.project_id
      WHERE pf.freelancer_id = :freelancerId
      ORDER BY p.deadline ASC
      """, nativeQuery = true)
  List<Project> findAllByFreelancerId(@org.springframework.data.repository.query.Param("freelancerId") Integer freelancerId);
}