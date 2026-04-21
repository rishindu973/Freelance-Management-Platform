package com.freelance.freelancepm.controller;

import com.freelance.freelancepm.dto.InvoiceCreateRequest;
import com.freelance.freelancepm.dto.InvoiceListDTO;
import com.freelance.freelancepm.dto.InvoiceResponse;
import com.freelance.freelancepm.dto.InvoiceUpdateRequest;
import com.freelance.freelancepm.dto.SendInvoiceRequest;
import com.freelance.freelancepm.service.IInvoiceService;
import com.freelance.freelancepm.service.InvoicePdfService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:5174" })
public class InvoiceController {

    private final IInvoiceService invoiceService;
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
    public ResponseEntity<Page<InvoiceListDTO>> list(
            @RequestParam(required = false) Integer clientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortProperty = switch (sortBy.toLowerCase()) {
            case "amount" -> "total";
            case "client" -> "client.name";
            default -> "createdAt"; // default to date
        };

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortProperty));

        return ResponseEntity.ok(invoiceService.listAll(clientId, startDate, endDate, pageable));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<?> downloadPdf(@PathVariable("id") Integer id) {
        log.info("[Download] PDF download requested for invoice ID: {}", id);
        try {
            InvoiceResponse invoice = invoiceService.getById(id);

            log.info("[Download] Generating PDF for invoice: {}, client: {}",
                    invoice.getInvoiceNumber(), invoice.getClientName());
            byte[] pdfBytes = invoicePdfService.generateInvoicePdf(id);

            String date = invoice.getCreatedAt() != null
                    ? invoice.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    : "N-A";

            // Null-safe sanitisation — prevents NPE when clientName or invoiceNumber is null
            String clientName = (invoice.getClientName() != null ? invoice.getClientName() : "Unknown")
                    .replaceAll("[^a-zA-Z0-9-]", "_");
            String invoiceNumber = (invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() : "DRAFT")
                    .replaceAll("[^a-zA-Z0-9-]", "_");

            String filename = String.format("Invoice_%s_%s_%s.pdf", invoiceNumber, clientName, date);
            log.info("[Download] Sending PDF file: {}, size: {} bytes", filename, pdfBytes.length);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (IllegalArgumentException e) {
            log.error("[Download] Invoice not found for PDF download, ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(java.util.Map.of(
                            "error", "NOT_FOUND",
                            "message", e.getMessage()));
        } catch (Exception e) {
            log.error("[Download] PDF generation failed for invoice ID: {} — {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(java.util.Map.of(
                            "error", "PDF_GENERATION_FAILED",
                            "message", "Failed to generate PDF. Please try again later.",
                            "detail", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    @PostMapping("/{id}/send")
    public ResponseEntity<Void> sendInvoice(
            @PathVariable("id") Integer id,
            @Valid @RequestBody SendInvoiceRequest req) {
        invoiceService.sendInvoice(id, req);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<InvoiceResponse> updateStatus(
            @PathVariable("id") Integer id,
            @Valid @RequestBody com.freelance.freelancepm.dto.InvoiceStatusUpdateRequest req) {
        return ResponseEntity.ok(invoiceService.updateStatus(id, req));
    }

    @PostMapping("/{id}/payments")
    public ResponseEntity<Void> addPayment(
            @PathVariable("id") Integer id,
            @Valid @RequestBody com.freelance.freelancepm.dto.PaymentCreateRequest req) {
        invoiceService.addPayment(id, req);
        return ResponseEntity.ok().build();
    }
}
