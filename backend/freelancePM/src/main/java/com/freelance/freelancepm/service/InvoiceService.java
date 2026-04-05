package com.freelance.freelancepm.service;

import com.freelance.freelancepm.dto.InvoiceCreateRequest;
import com.freelance.freelancepm.dto.InvoiceLineItemRequest;
import com.freelance.freelancepm.dto.InvoiceResponse;
import com.freelance.freelancepm.dto.InvoiceUpdateRequest;
import com.freelance.freelancepm.entity.Invoice;
import com.freelance.freelancepm.entity.InvoiceLineItem;
import com.freelance.freelancepm.entity.Project;
import com.freelance.freelancepm.exception.ConflictException;
import com.freelance.freelancepm.exception.NotFoundException;
import com.freelance.freelancepm.mapper.InvoiceMapper;
import com.freelance.freelancepm.model.Client;
import com.freelance.freelancepm.repository.ClientRepository;
import com.freelance.freelancepm.repository.InvoiceRepository;
import com.freelance.freelancepm.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceService implements IInvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;
    private final ProjectRepository projectRepository;
    private final InvoiceMapper invoiceMapper;

    @Override
    @Transactional
    public InvoiceResponse create(InvoiceCreateRequest req) {
        Client client = findClient(req.getClientId());
        Project project = req.getProjectId() != null ? findProject(req.getProjectId()) : null;

        Invoice invoice = Invoice.builder()
                .client(client)
                .project(project)
                .status(req.getStatus() != null ? req.getStatus() : Invoice.Status.DRAFT)
                .lineItems(new ArrayList<>())
                .build();

        updateLineItems(invoice, req.getLineItems());
        return saveAndHandleConflict(invoice);
    }

    @Override
    @Transactional
    public InvoiceResponse update(Integer invoiceId, InvoiceUpdateRequest req) {
        Invoice invoice = findInvoice(invoiceId);
        ensureDraftStatus(invoice);

        if (req.getClientId() != null) {
            invoice.setClient(findClient(req.getClientId()));
        }
        if (req.getProjectId() != null) {
            invoice.setProject(findProject(req.getProjectId()));
        }
        if (req.getStatus() != null) {
            invoice.setStatus(req.getStatus());
        }
        if (req.getLineItems() != null) {
            updateLineItems(invoice, req.getLineItems());
        }

        return saveAndHandleConflict(invoice);
    }

    @Override
    public InvoiceResponse getById(Integer invoiceId) {
        return invoiceMapper.toResponse(findInvoice(invoiceId));
    }

    @Override
    public List<InvoiceResponse> list() {
        return invoiceRepository.findAll().stream()
                .map(invoiceMapper::toResponse)
                .toList();
    }

    private void updateLineItems(Invoice invoice, List<InvoiceLineItemRequest> itemRequests) {
        if (itemRequests == null)
            return;

        invoice.getLineItems().clear();
        for (InvoiceLineItemRequest itemReq : itemRequests) {
            validateLineItemRequest(itemReq);
            invoice.addLineItem(invoiceMapper.toLineItemEntity(itemReq));
        }
        invoice.recalculateTotals();
    }

    private InvoiceResponse saveAndHandleConflict(Invoice invoice) {
        validateInvoiceBusinessRules(invoice);
        try {
            Invoice saved = invoiceRepository.save(invoice);
            return invoiceMapper.toResponse(saved);
        } catch (ObjectOptimisticLockingFailureException ex) {
            throw new ConflictException("The invoice was updated by another user. Please refresh and try again.");
        }
    }

    private void validateInvoiceBusinessRules(Invoice invoice) {
        if (invoice.getClient() == null) {
            throw new IllegalArgumentException("Invoice must be associated with a client");
        }

        if (invoice.getProject() != null && invoice.getProject().getClient() != null &&
                !invoice.getProject().getClient().getId().equals(invoice.getClient().getId())) {
            throw new IllegalArgumentException("Project does not belong to the selected client");
        }

        if (Invoice.Status.FINAL.equals(invoice.getStatus())) {
            validateFinalizedInvoice(invoice);
        }
    }

    private void validateFinalizedInvoice(Invoice invoice) {
        if (invoice.getLineItems() == null || invoice.getLineItems().isEmpty()) {
            throw new IllegalStateException("Finalized invoices must have at least one line item");
        }
        for (InvoiceLineItem item : invoice.getLineItems()) {
            if (item.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalStateException("Finalized invoices cannot have items with zero or negative amounts");
            }
        }
    }

    private void validateLineItemRequest(InvoiceLineItemRequest req) {
        if (req.getQuantity() < 0 || req.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Quantity and unit price cannot be negative");
        }
    }

    private void ensureDraftStatus(Invoice invoice) {
        if (!Invoice.Status.DRAFT.equals(invoice.getStatus())) {
            throw new IllegalStateException("Only DRAFT invoices can be updated");
        }
    }

    private Client findClient(Integer clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new NotFoundException("Client not found"));
    }

    private Project findProject(Integer projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found"));
    }

    private Invoice findInvoice(Integer invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException("Invoice not found"));
    }
}
