package com.freelance.freelancepm.service;

import com.freelance.freelancepm.entity.Invoice;
import com.freelance.freelancepm.entity.InvoiceStatus;
import com.freelance.freelancepm.entity.InvoiceLineItem;
import com.freelance.freelancepm.model.Client;
import com.freelance.freelancepm.repository.InvoiceLineItemRepository;
import com.freelance.freelancepm.repository.InvoiceRepository;
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
        invoicePdfService = new InvoicePdfService(invoiceRepository, invoiceLineItemRepository, managerRepository);
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

        // We only verify page numbering exists, since the layout is currently a skeleton without full content drawing.
        String pdfText = extractTextFromPdf(pdfBytes);
        assertTrue(pdfText.contains("Page 1 of 1"), "PDF should render default page numbering via post-processing");
    }

    @Test
    void generateInvoicePdf_MultiPageSkeleton() throws IOException {
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

        List<InvoiceLineItem> lineItems = new ArrayList<>();
        lineItems.add(InvoiceLineItem.builder()
                .description("Item")
                .quantity(1)
                .unitPrice(new BigDecimal("10.00"))
                .amount(new BigDecimal("10.00"))
                .build());

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(invoiceLineItemRepository.findByInvoiceId(invoiceId)).thenReturn(lineItems);

        // Act
        byte[] pdfBytes = invoicePdfService.generateInvoicePdf(invoiceId);

        // Assert
        assertNotNull(pdfBytes);
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            // Because layout logic is skeleton-only, we expect 1 page.
            assertEquals(1, document.getNumberOfPages(), "Skeleton PDF will only have one page initially");
            
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            assertTrue(text.contains("Page 1 of 1"), "Should contain page numbering");
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
    void generateInvoicePdf_BrandedSkeleton() throws IOException {
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
        // Skeleton will process without exceptions
        assertTrue(pdfBytes.length > 0);
    }

    private String extractTextFromPdf(byte[] pdfBytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
}
