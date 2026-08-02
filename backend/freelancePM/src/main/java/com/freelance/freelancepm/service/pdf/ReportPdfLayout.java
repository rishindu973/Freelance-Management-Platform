package com.freelance.freelancepm.service.pdf;

import java.io.IOException;
import java.util.List;

import com.freelance.freelancepm.entity.Manager;
import com.freelance.freelancepm.dto.ReportResponse;
import com.freelance.freelancepm.service.ReportPdfService.ProjectRevenueDetails;

import java.time.LocalDate;
import java.util.List;

public interface ReportPdfLayout {
    void drawHeader(PdfGenerationContext context, LocalDate startDate, LocalDate endDate, Manager manager,
            byte[] logoBytes) throws IOException;

    void drawSummaryCards(PdfGenerationContext context, ReportResponse report) throws IOException;

    void drawBreakdownTable(PdfGenerationContext context, List<ProjectRevenueDetails> projectRevenues)
            throws IOException;

    void drawFooter(PdfGenerationContext context, Manager manager) throws IOException;
}
