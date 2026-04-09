package com.freelance.freelancepm.service;

import com.freelance.freelancepm.dto.DailyRevenue;
import com.freelance.freelancepm.dto.ReportResponse;
import com.freelance.freelancepm.entity.Payment;
import com.freelance.freelancepm.repository.InvoiceRepository;
import com.freelance.freelancepm.repository.PaymentRepository;
import com.freelance.freelancepm.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final PaymentRepository paymentRepository;
    private final ProjectRepository projectRepository;
    private final InvoiceRepository invoiceRepository;

    public ReportResponse getReport(LocalDate startDate, LocalDate endDate) {
        long projectsStarted = projectRepository.countByStartDateBetween(startDate, endDate);
        long projectsCompleted = projectRepository.countCompletedInDateRange(startDate, endDate);
        long invoicesGenerated = invoiceRepository.countByCreatedAtBetween(
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59));

        List<Payment> payments = paymentRepository.findByPaymentDateBetween(startDate, endDate);

        BigDecimal totalRevenue = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<LocalDate, BigDecimal> dailyMap = payments.stream()
                .collect(Collectors.groupingBy(
                        Payment::getPaymentDate,
                        Collectors.reducing(BigDecimal.ZERO, Payment::getAmount, BigDecimal::add)));

        List<DailyRevenue> timeline = startDate.datesUntil(endDate.plusDays(1))
                .map(date -> new DailyRevenue(date, dailyMap.getOrDefault(date, BigDecimal.ZERO)))
                .collect(Collectors.toList());

        return ReportResponse.builder()
                .totalRevenue(totalRevenue)
                .projectsStarted(projectsStarted)
                .projectsCompleted(projectsCompleted)
                .invoicesGenerated(invoicesGenerated)
                .revenueTimeline(timeline)
                .build();
    }
}
