package com.freelance.freelancepm.service;

import com.freelance.freelancepm.dto.InvoiceCreateRequest;
import com.freelance.freelancepm.dto.InvoiceLineItemRequest;
import com.freelance.freelancepm.dto.InvoiceLineItemResponse;
import com.freelance.freelancepm.dto.InvoiceResponse;
import com.freelance.freelancepm.dto.InvoiceUpdateRequest;
import com.freelance.freelancepm.entity.Invoice;
import com.freelance.freelancepm.entity.InvoiceLineItem;
import com.freelance.freelancepm.entity.Project;
import com.freelance.freelancepm.exception.NotFoundException;
import com.freelance.freelancepm.model.Client;
import com.freelance.freelancepm.repository.ClientRepository;
import com.freelance.freelancepm.repository.InvoiceRepository;
import com.freelance.freelancepm.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;
    private final ProjectRepository projectRepository;

    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");

    @Transactional
    public InvoiceResponse create(InvoiceCreateRequest req) {
        Client client = clientRepository.findById(req.getClientId())
                .orElseThrow(() -> new NotFoundException("Client not found"));

        Project project = null;
        if (req.getProjectId() != null) {
            project = projectRepository.findById(req.getProjectId())
                    .orElseThrow(() -> new NotFoundException("Project not found"));
        }

        Invoice invoice = Invoice.builder()
                .client(client)
                .project(project)
                .status(req.getStatus() != null ? req.getStatus() : Invoice.Status.DRAFT)
                .lineItems(new ArrayList<>())
                .build();

        // Calculate line items and totals
        BigDecimal subtotal = BigDecimal.ZERO;

        for (InvoiceLineItemRequest itemReq : req.getLineItems()) {
            BigDecimal amount = itemReq.getUnitPrice().multiply(new BigDecimal(itemReq.getQuantity()));
            subtotal = subtotal.add(amount);

            InvoiceLineItem item = InvoiceLineItem.builder()
                    .description(itemReq.getDescription())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(itemReq.getUnitPrice())
                    .amount(amount)  // Trust calculation over request payload
                    .build();

            invoice.addLineItem(item);
        }

        BigDecimal tax = subtotal.multiply(TAX_RATE);
        BigDecimal total = subtotal.add(tax);

        invoice.setSubtotal(subtotal);
        invoice.setTax(tax);
        invoice.setTotal(total);

        validateInvoice(invoice);

        Invoice saved = invoiceRepository.save(invoice);
        return toResponse(saved);
    }

    @Transactional
    public InvoiceResponse update(Long invoiceId, InvoiceUpdateRequest req) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException("Invoice not found"));

        if (!Invoice.Status.DRAFT.equals(invoice.getStatus())) {
            throw new IllegalStateException("Only DRAFT invoices can be updated");
        }

        if (req.getClientId() != null) {
            Client client = clientRepository.findById(req.getClientId())
                    .orElseThrow(() -> new NotFoundException("Client not found"));
            invoice.setClient(client);
        }

        if (req.getProjectId() != null) {
            Project project = projectRepository.findById(req.getProjectId())
                    .orElseThrow(() -> new NotFoundException("Project not found"));
            invoice.setProject(project);
        }

        if (req.getStatus() != null) {
            invoice.setStatus(req.getStatus());
        }

        if (req.getLineItems() != null) {
            // Orphan removal will delete old line items cleanly
            invoice.getLineItems().clear(); 
            BigDecimal subtotal = BigDecimal.ZERO;

            for (InvoiceLineItemRequest itemReq : req.getLineItems()) {
                BigDecimal amount = itemReq.getUnitPrice().multiply(new BigDecimal(itemReq.getQuantity()));
                subtotal = subtotal.add(amount);

                InvoiceLineItem item = InvoiceLineItem.builder()
                        .description(itemReq.getDescription())
                        .quantity(itemReq.getQuantity())
                        .unitPrice(itemReq.getUnitPrice())
                        .amount(amount)
                        .build();

                invoice.addLineItem(item);
            }

            BigDecimal tax = subtotal.multiply(TAX_RATE);
            BigDecimal total = subtotal.add(tax);

            invoice.setSubtotal(subtotal);
            invoice.setTax(tax);
            invoice.setTotal(total);
        }

        validateInvoice(invoice);

        return toResponse(invoiceRepository.save(invoice));
    }

    public InvoiceResponse getById(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException("Invoice not found"));
        return toResponse(invoice);
    }

    public java.util.List<InvoiceResponse> list() {
        return invoiceRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private void validateInvoice(Invoice invoice) {
        if (Invoice.Status.FINAL.equals(invoice.getStatus()) && invoice.getLineItems().isEmpty()) {
            throw new IllegalArgumentException("FINAL invoice must have at least one line item");
        }
    }

    private InvoiceResponse toResponse(Invoice invoice) {
        InvoiceResponse response = new InvoiceResponse();
        response.setId(invoice.getId());
        response.setClientId(invoice.getClient().getId());
        response.setProjectId(invoice.getProject() != null ? invoice.getProject().getId() : null);
        response.setStatus(invoice.getStatus());
        response.setSubtotal(invoice.getSubtotal());
        response.setTax(invoice.getTax());
        response.setTotal(invoice.getTotal());
        response.setCreatedAt(invoice.getCreatedAt());
        response.setUpdatedAt(invoice.getUpdatedAt());

        if (invoice.getLineItems() != null) {
            response.setLineItems(invoice.getLineItems().stream().map(item -> {
                InvoiceLineItemResponse itemRes = new InvoiceLineItemResponse();
                itemRes.setId(item.getId());
                itemRes.setDescription(item.getDescription());
                itemRes.setQuantity(item.getQuantity());
                itemRes.setUnitPrice(item.getUnitPrice());
                itemRes.setAmount(item.getAmount());
                return itemRes;
            }).collect(Collectors.toList()));
        }

        return response;
    }
}
