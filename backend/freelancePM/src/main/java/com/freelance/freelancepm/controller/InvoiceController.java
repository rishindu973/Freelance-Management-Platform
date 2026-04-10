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
import org.springframework.http.HttpHeaders;
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
