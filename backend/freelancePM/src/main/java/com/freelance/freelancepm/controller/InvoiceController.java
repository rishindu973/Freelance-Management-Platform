package com.freelance.freelancepm.controller;

import com.freelance.freelancepm.dto.InvoiceCreateRequest;
import com.freelance.freelancepm.dto.InvoiceResponse;
import com.freelance.freelancepm.dto.InvoiceUpdateRequest;
import com.freelance.freelancepm.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:5174" })
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    public ResponseEntity<InvoiceResponse> create(@Valid @RequestBody InvoiceCreateRequest req) {
        return ResponseEntity.ok(invoiceService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvoiceResponse> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody InvoiceUpdateRequest req) {
        return ResponseEntity.ok(invoiceService.update(id, req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> get(@PathVariable("id") Long id) {
        return ResponseEntity.ok(invoiceService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<InvoiceResponse>> list() {
        return ResponseEntity.ok(invoiceService.list());
    }
}
