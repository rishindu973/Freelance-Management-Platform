package com.freelance.freelancepm.service;

import com.freelance.freelancepm.entity.Project;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class ProjectSpecifications {

    public static Specification<Project> managerIdEquals(Integer managerId) {
        return (root, query, cb) -> cb.equal(root.get("managerId"), managerId);
    }

    public static Specification<Project> statusEquals(String status) {
        return (root, query, cb) -> cb.equal(cb.lower(root.get("status")), status.toLowerCase());
    }

    public static Specification<Project> clientIdEquals(Integer clientId) {
        return (root, query, cb) -> cb.equal(root.get("clientId"), clientId);
    }

    public static Specification<Project> nameOrDescriptionContains(String q) {
        String like = "%" + q.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), like),
                cb.like(cb.lower(root.get("description")), like));
    }

    public static Specification<Project> deadlineBetween(LocalDate from, LocalDate to) {
        return (root, query, cb) -> cb.between(root.get("deadline"), from, to);
    }

    public static Specification<Project> isCritical(LocalDate now) {
        LocalDate sevenDaysFromNow = now.plusDays(7);
        return (root, query, cb) -> cb.and(
                cb.between(root.get("deadline"), now, sevenDaysFromNow),
                cb.notEqual(cb.lower(root.get("status")), "completed"));
    }
}