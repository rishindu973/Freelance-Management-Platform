package com.freelance.freelancepm.controller;

import com.freelance.freelancepm.dto.InvoiceCreateRequest;
import com.freelance.freelancepm.dto.InvoiceResponse;
import com.freelance.freelancepm.dto.InvoiceUpdateRequest;
import com.freelance.freelancepm.service.InvoicePdfService;
import com.freelance.freelancepm.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:5174" })
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final InvoicePdfService invoicePdfService;

    @PostMapping
    public ResponseEntity<InvoiceResponse> create(@Valid @RequestBody InvoiceCreateRequest req) {
        return ResponseEntity.ok(invoiceService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvoiceResponse> update(
            @PathVariable("id") Integer id,
            @Valid @RequestBody InvoiceUpdateRequest req) {
        return ResponseEntity.ok(invoiceService.update(id, req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> get(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(invoiceService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<InvoiceResponse>> list() {
        return ResponseEntity.ok(invoiceService.list());
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable("id") Integer id) {
        InvoiceResponse invoice = invoiceService.getById(id);
        byte[] pdfBytes = invoicePdfService.generateInvoicePdf(id);

        String date = invoice.getCreatedAt() != null 
                ? invoice.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) 
                : "N-A";
        
        // Sanitize filename components (simple version)
        String clientName = invoice.getClientName().replaceAll("[^a-zA-Z0-9-]", "_");
        String invoiceNumber = invoice.getInvoiceNumber().replaceAll("[^a-zA-Z0-9-]", "_");
        
        String filename = String.format("Invoice_%s_%s_%s.pdf", invoiceNumber, clientName, date);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
