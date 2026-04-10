package com.freelance.freelancepm.service;

import com.freelance.freelancepm.entity.Invoice;
import com.freelance.freelancepm.entity.InvoiceStatus;
import com.freelance.freelancepm.entity.InvoiceLineItem;
import com.freelance.freelancepm.model.Client;
import com.freelance.freelancepm.repository.InvoiceLineItemRepository;
import com.freelance.freelancepm.repository.InvoiceRepository;
import com.freelance.freelancepm.service.pdf.StandardInvoicePdfLayout;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoicePdfServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceLineItemRepository invoiceLineItemRepository;

    @Mock
    private com.freelance.freelancepm.repository.ManagerRepository managerRepository;

    private InvoicePdfService invoicePdfService;

    @BeforeEach
    void setUp() {
        // We use the real standard layout to verify the actual PDF content
        StandardInvoicePdfLayout layout = new StandardInvoicePdfLayout();
        invoicePdfService = new InvoicePdfService(invoiceRepository, invoiceLineItemRepository, managerRepository, layout);
    }

    @Test
    void generateInvoicePdf_Successful() throws IOException {
        // Arrange
        Integer invoiceId = 1;
        String invoiceNumber = "INV-2024-001";
        
        Client client = new Client();
        client.setId(1);
        client.setName("Test Client");
        client.setEmail("test@client.com");

        Invoice invoice = Invoice.builder()
                .id(invoiceId)
                .invoiceNumber(invoiceNumber)
                .client(client)
                .status(InvoiceStatus.FINAL)
                .subtotal(new BigDecimal("100.00"))
                .tax(new BigDecimal("10.00"))
                .total(new BigDecimal("110.00"))
                .createdAt(LocalDateTime.now())
                .lineItems(new ArrayList<>())
                .build();

        List<InvoiceLineItem> lineItems = new ArrayList<>();
        lineItems.add(InvoiceLineItem.builder()
                .description("Consulting Service")
                .quantity(1)
                .unitPrice(new BigDecimal("100.00"))
                .amount(new BigDecimal("100.00"))
                .build());

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(invoiceLineItemRepository.findByInvoiceId(invoiceId)).thenReturn(lineItems);

        // Act
        byte[] pdfBytes = invoicePdfService.generateInvoicePdf(invoiceId);

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0, "PDF byte array should not be empty");

        // Verify PDF content contains the invoice number
        String pdfText = extractTextFromPdf(pdfBytes);
        assertTrue(pdfText.contains(invoiceNumber), "PDF should contain invoice number: " + invoiceNumber);
        assertTrue(pdfText.contains("Test Client"), "PDF should contain client name: Test Client");
        assertTrue(pdfText.contains("Consulting Service"), "PDF should contain line item description");
    }

    @Test
    void generateInvoicePdf_MultiPage() throws IOException {
        // Arrange
        Integer invoiceId = 2;
        Client client = new Client();
        client.setName("Big Enterprise");

        Invoice invoice = Invoice.builder()
                .id(invoiceId)
                .invoiceNumber("INV-MULTI-001")
                .client(client)
                .status(InvoiceStatus.FINAL)
                .createdAt(LocalDateTime.now())
                .total(new BigDecimal("1000.00"))
                .build();

        // Add 30 line items to definitely trigger a second page
        List<InvoiceLineItem> lineItems = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            lineItems.add(InvoiceLineItem.builder()
                    .description("Service Item #" + i + " with a very long description to consume more vertical space in the document layout")
                    .quantity(1)
                    .unitPrice(new BigDecimal("10.00"))
                    .amount(new BigDecimal("10.00"))
                    .build());
        }

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(invoiceLineItemRepository.findByInvoiceId(invoiceId)).thenReturn(lineItems);

        // Act
        byte[] pdfBytes = invoicePdfService.generateInvoicePdf(invoiceId);

        // Assert
        assertNotNull(pdfBytes);
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            assertTrue(document.getNumberOfPages() > 1, "PDF should have more than one page");
            
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            assertTrue(text.contains("Page 1 of"), "Should contain page numbering");
            assertTrue(text.contains("Page 2 of"), "Should contain page numbering on page 2");
        }
    }

    @Test
    void generateInvoicePdf_NotFound_ThrowsException() {
        // Arrange
        Integer invoiceId = 999;
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> invoicePdfService.generateInvoicePdf(invoiceId));
    }

    @Test
    void generateInvoicePdf_Branded() throws IOException {
        // Arrange
        Integer invoiceId = 3;
        String customBrand = "Antigravity Premium Corp";
        
        com.freelance.freelancepm.entity.Manager manager = new com.freelance.freelancepm.entity.Manager();
        manager.setCompanyName(customBrand);
        manager.setBrandingColor("#FF0000"); // Red

        com.freelance.freelancepm.entity.Project project = new com.freelance.freelancepm.entity.Project();
        project.setManagerId(1);

        com.freelance.freelancepm.model.Client client = new com.freelance.freelancepm.model.Client();
        client.setName("Elite Client");

        Invoice invoice = Invoice.builder()
                .id(invoiceId)
                .invoiceNumber("INV-CUSTOM-001")
                .client(client)
                .project(project)
                .status(InvoiceStatus.FINAL)
                .createdAt(LocalDateTime.now())
                .total(new BigDecimal("500.00"))
                .build();

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(invoiceLineItemRepository.findByInvoiceId(invoiceId)).thenReturn(new java.util.ArrayList<>());
        when(managerRepository.findById(1)).thenReturn(Optional.of(manager));

        // Act
        byte[] pdfBytes = invoicePdfService.generateInvoicePdf(invoiceId);

        // Assert
        assertNotNull(pdfBytes);
        String text = extractTextFromPdf(pdfBytes);
        assertTrue(text.contains(customBrand), "PDF should contain custom company name: " + customBrand);
    }

    private String extractTextFromPdf(byte[] pdfBytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
}
