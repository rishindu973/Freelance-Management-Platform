package com.freelance.freelancepm.service.pdf;

import java.io.IOException;
import java.util.List;

import com.freelance.freelancepm.entity.Manager;
import com.freelance.freelancepm.entity.Report;
import com.freelance.freelancepm.entity.ReportDetail;

public interface ReportPdfLayout {
    void drawHeader(PdfGenerationContext context, Report report, Manager manager, byte[] logoBytes) throws IOException;

    void drawSummaryCards(PdfGenerationContext context, Report report) throws IOException;

    void drawBreakdownTable(PdfGenerationContext context, List<ReportDetail> details) throws IOException;

    void drawFooter(PdfGenerationContext context, Manager manager) throws IOException;
}
