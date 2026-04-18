package com.freelance.freelancepm.service;

import com.freelance.freelancepm.entity.Manager;
import com.freelance.freelancepm.entity.Report;
import com.freelance.freelancepm.repository.ManagerRepository;
import com.freelance.freelancepm.repository.ReportRepository;
import com.freelance.freelancepm.service.pdf.PdfGenerationContext;
import com.freelance.freelancepm.service.pdf.PdfStyle;
import com.freelance.freelancepm.service.pdf.ReportPdfLayout;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ReportPdfService {

    private final ReportRepository reportRepository;
    private final ManagerRepository managerRepository;
    private final ReportPdfLayout reportLayout;

    public byte[] generateReportPdf(Integer reportId) {

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        Manager manager = managerRepository.findAll().stream().findFirst().orElse(null);

        try (PDDocument document = new PDDocument();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfStyle style = PdfStyle.fromManager(manager);
            try (PdfGenerationContext context = new PdfGenerationContext(document, style)) {
                reportLayout.drawHeader(context, report, manager, null);
                reportLayout.drawSummaryCards(context, report);
                reportLayout.drawBreakdownTable(context, report.getDetails());
                reportLayout.drawFooter(context, manager);
            }
            document.save(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Report PDF", e);
        }
    }
}