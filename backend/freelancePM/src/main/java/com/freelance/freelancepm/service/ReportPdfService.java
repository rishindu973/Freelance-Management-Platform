package com.freelance.freelancepm.service;

import com.freelance.freelancepm.entity.Manager;
import com.freelance.freelancepm.entity.Payment;
import com.freelance.freelancepm.dto.ReportResponse;
import com.freelance.freelancepm.repository.ManagerRepository;
import com.freelance.freelancepm.repository.PaymentRepository;
import com.freelance.freelancepm.service.pdf.PdfGenerationContext;
import com.freelance.freelancepm.service.pdf.PdfStyle;
import com.freelance.freelancepm.service.pdf.ReportPdfLayout;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ReportPdfService {

    private final ReportService reportService;
    private final PaymentRepository paymentRepository;
    private final ManagerRepository managerRepository;
    private final ReportPdfLayout reportLayout;

    public record ProjectRevenueDetails(String projectName, String clientName, BigDecimal revenue) {
    }

    @Transactional(readOnly = true)
    public byte[] generateReportPdf(LocalDate startDate, LocalDate endDate) {

        ReportResponse reportData = reportService.getReport(startDate, endDate);
        Manager manager = managerRepository.findAll().stream().findFirst().orElse(null);
        List<Payment> payments = paymentRepository.findByPaymentDateBetween(
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59));

        Map<String, ProjectRevenueDetails> breakdownMap = new HashMap<>();

        for (Payment payment : payments) {
            String projectName = "Unknown Project";
            String clientName = "Unknown Client";

            if (payment.getInvoice() != null && payment.getInvoice().getProjects() != null && !payment.getInvoice().getProjects().isEmpty()) {
                projectName = payment.getInvoice().getProjects().stream().map(p -> p.getName()).collect(Collectors.joining(", "));
                if (payment.getInvoice().getClient() != null) {
                    clientName = payment.getInvoice().getClient().getName();
                }
            }

            String key = projectName + "||" + clientName;
            ProjectRevenueDetails current = breakdownMap.getOrDefault(key,
                    new ProjectRevenueDetails(projectName, clientName, BigDecimal.ZERO));
            breakdownMap.put(key,
                    new ProjectRevenueDetails(projectName, clientName, current.revenue().add(payment.getAmount())));
        }

        List<ProjectRevenueDetails> projectBreakdown = new ArrayList<>(breakdownMap.values());
        projectBreakdown.sort((a, b) -> b.revenue().compareTo(a.revenue())); // Sort by highest revenue

        try (PDDocument document = new PDDocument();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfStyle style = PdfStyle.fromManager(manager);
            try (PdfGenerationContext context = new PdfGenerationContext(document, style)) {
                reportLayout.drawHeader(context, startDate, endDate, manager, null);
                reportLayout.drawSummaryCards(context, reportData);
                reportLayout.drawBreakdownTable(context, projectBreakdown);
                reportLayout.drawFooter(context, manager);
            }
            document.save(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Report PDF", e);
        }
    }
}