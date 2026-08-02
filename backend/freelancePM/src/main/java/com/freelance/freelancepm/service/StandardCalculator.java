package com.freelance.freelancepm.service;

import com.freelance.freelancepm.entity.Project;
import com.freelance.freelancepm.entity.Report;
import com.freelance.freelancepm.entity.ReportDetail;
import com.freelance.freelancepm.repository.FinancialCalculator;
import com.freelance.freelancepm.repository.PaymentRepository;
import com.freelance.freelancepm.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StandardCalculator implements FinancialCalculator {
    private final PaymentRepository paymentRepository;
    private final ProjectRepository projectRepository;

    @Override
    public Report calculateFinancials(LocalDate start, LocalDate end) {
        Report report = Report.builder()
                .type(Report.ReportType.WEEKLY)
                .build();
        List<Project> activeProjects = projectRepository.findByStartDateBetween(start, end);

        for (Project project : activeProjects) {
            BigDecimal income = paymentRepository.sumIncomeByProject(project.getId(), start.atStartOfDay(),
                    end.atTime(23, 59, 59));
            income = (income != null) ? income : BigDecimal.ZERO;

            BigDecimal estimatedExpense = income.multiply(new BigDecimal("0.40"));

            ReportDetail detail = ReportDetail.builder()
                    .project(project)
                    .client(project.getClient())
                    .income(income)
                    .expense(estimatedExpense)
                    .profit(income.subtract(estimatedExpense))
                    .build();
            report.addDetail(detail);
        }
        report.syncTotals();
        return report;

    }

}
