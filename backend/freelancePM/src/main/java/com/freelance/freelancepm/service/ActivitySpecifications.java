package com.freelance.freelancepm.service;

import com.freelance.freelancepm.entity.Activity;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class ActivitySpecifications {

    public static Specification<Activity> managerIdEquals(Long managerId) {
        return (root, query, cb) -> cb.equal(root.get("managerId"), managerId);
    }

    public static Specification<Activity> typeEquals(Activity.ActivityType type) {
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    public static Specification<Activity> timestampBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> cb.between(root.get("timestamp"), from, to);
    }
    
    public static Specification<Activity> timestampGreaterThanEqual(LocalDateTime from) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("timestamp"), from);
    }

    public static Specification<Activity> timestampLessThanEqual(LocalDateTime to) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("timestamp"), to);
    }
}
