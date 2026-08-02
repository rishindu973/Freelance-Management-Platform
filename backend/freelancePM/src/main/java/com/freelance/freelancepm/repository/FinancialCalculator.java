package com.freelance.freelancepm.repository;

import java.time.LocalDate;

import com.freelance.freelancepm.entity.Report;

public interface FinancialCalculator {
    Report calculateFinancials(LocalDate start, LocalDate end);
}
