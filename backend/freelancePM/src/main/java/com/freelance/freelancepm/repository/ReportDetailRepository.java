package com.freelance.freelancepm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.freelance.freelancepm.entity.ReportDetail;

@Repository
public interface ReportDetailRepository extends JpaRepository<ReportDetail, Integer> {

}
