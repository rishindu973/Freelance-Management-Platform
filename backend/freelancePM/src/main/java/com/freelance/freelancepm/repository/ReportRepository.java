package com.freelance.freelancepm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.freelance.freelancepm.entity.Report;

@Repository
public interface ReportRepository extends JpaRepository<Report, Integer> {
    List<Report> findByType(Report.ReportType type);
}
