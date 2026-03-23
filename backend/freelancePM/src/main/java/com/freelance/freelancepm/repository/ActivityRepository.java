package com.freelance.freelancepm.repository;

import com.freelance.freelancepm.entity.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long>, JpaSpecificationExecutor<Activity> {

    /**
     * Finds activities for a specific manager with pagination.
     */
    Page<Activity> findByManagerId(Long managerId, Pageable pageable);

    /**
     * Filters activities by a specific manager and activity type, with pagination.
     */
    Page<Activity> findByManagerIdAndType(Long managerId, Activity.ActivityType type, Pageable pageable);

    /**
     * Filters activities by a manager and a specific timestamp range, with pagination.
     */
    Page<Activity> findByManagerIdAndTimestampBetween(Long managerId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Filters activities by manager, exact type, and timestamp range, with pagination.
     */
    Page<Activity> findByManagerIdAndTypeAndTimestampBetween(Long managerId, Activity.ActivityType type, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

}
