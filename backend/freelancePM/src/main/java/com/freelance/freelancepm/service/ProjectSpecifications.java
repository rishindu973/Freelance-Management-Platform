package com.freelance.freelancepm.service;

import com.freelance.freelancepm.domain.Project;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class ProjectSpecifications {

    public static Specification<Project> managerIdEquals(Long managerId) {
        return (root, query, cb) -> cb.equal(root.get("managerId"), managerId);
    }

    public static Specification<Project> statusEquals(String status) {
        return (root, query, cb) -> cb.equal(cb.lower(root.get("status")), status.toLowerCase());
    }

    public static Specification<Project> clientIdEquals(Long clientId) {
        return (root, query, cb) -> cb.equal(root.get("clientId"), clientId);
    }

    public static Specification<Project> nameOrDescriptionContains(String q) {
        String like = "%" + q.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), like),
                cb.like(cb.lower(root.get("description")), like)
        );
    }

    public static Specification<Project> deadlineBetween(LocalDate from, LocalDate to) {
        return (root, query, cb) -> cb.between(root.get("deadline"), from, to);
    }
}