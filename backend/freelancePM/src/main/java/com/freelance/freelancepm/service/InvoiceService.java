package com.freelance.freelancepm.service;

import com.freelance.freelancepm.dto.*;
import com.freelance.freelancepm.entity.*;
import com.freelance.freelancepm.exception.ConflictException;
import com.freelance.freelancepm.exception.NotFoundException;
import com.freelance.freelancepm.mapper.InvoiceMapper;
import com.freelance.freelancepm.model.Client;
import com.freelance.freelancepm.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService implements IInvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;
    private final ProjectRepository projectRepository;
    private final ManagerRepository managerRepository;
    private final ClientInvoiceSequenceRepository sequenceRepository;
    private final InvoiceMapper invoiceMapper;
    private final EmailDispatcherService emailDispatcherService;
    private final InvoicePdfService pdfService;
    private final InvoiceEditValidator editValidator;
    private final InvoiceCalculationService calculationService;

    private boolean regeneratePdfOnUpdate = true; // Configuration flag

    @Override
    @Transactional
    public InvoiceResponse create(InvoiceCreateRequest req) {
        Client client = findClient(req.getClientId());
        Project project = req.getProjectId() != null ? findProject(req.getProjectId()) : null;

        Invoice invoice = Invoice.builder()
                .client(client)
                .project(project)
                .status(req.getStatus() != null ? req.getStatus() : InvoiceStatus.DRAFT)
                .lineItems(new ArrayList<>())
                .year(LocalDate.now().getYear())
                .build();

        assignInvoiceNumber(invoice);
        updateLineItems(invoice, req.getLineItems());
        return saveAndHandleConflict(invoice);
    }

    @Override
    @Transactional
    public InvoiceResponse update(Integer invoiceId, InvoiceUpdateRequest req) {
        Invoice invoice = findInvoice(invoiceId);
        editValidator.validateEditable(invoice);

        if (req.getClientId() != null && !req.getClientId().equals(invoice.getClient().getId())) {
            invoice.setClient(findClient(req.getClientId()));
            assignInvoiceNumber(invoice);
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

        InvoiceResponse response = saveAndHandleConflict(invoice);

        if (regeneratePdfOnUpdate) {
            try {
                pdfService.generateInvoicePdf(invoice.getId());
                log.info("PDF regenerated for invoice ID: {}", invoice.getId());
            } catch (Exception e) {
                log.error("Failed to automatically regenerate PDF for invoice ID: {}", invoice.getId(), e);
            }
        }

        return response;
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

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceListDTO> listAll(Integer clientId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must not be after endDate");
        }

        Specification<Invoice> spec = Specification.where((root, query, criteriaBuilder) -> criteriaBuilder.conjunction());

        if (clientId != null) {
            spec = spec.and(InvoiceSpecifications.clientIdEquals(clientId));
        }
        if (startDate != null) {
            spec = spec.and(InvoiceSpecifications.createdOnOrAfter(startDate.atStartOfDay()));
        }
        if (endDate != null) {
            spec = spec.and(InvoiceSpecifications.createdOnOrBefore(endDate.plusDays(1).atStartOfDay()));
        }

        return invoiceRepository.findAll(spec, pageable)
                .map(invoiceMapper::toListDTO);
    }

    @Override
    @Transactional
    public void sendInvoice(Integer invoiceId, SendInvoiceRequest request) {
        Invoice invoice = findInvoice(invoiceId);
        
        // Idempotency check: Don't re-send if already SENT (optional: or FAILED if you want to allow retrying failed ones)
        if (InvoiceStatus.SENT.equals(invoice.getStatus())) {
            log.warn("Invoice {} is already marked as SENT. Skipping dispatch.", invoiceId);
            return;
        }

        log.info("Handing off invoice {} dispatch to async dispatcher", invoiceId);

        // 1. Prepare data for async processing
        byte[] pdfBytes = pdfService.generateInvoicePdf(invoiceId);
        String filename = String.format("Invoice_%s.pdf", invoice.getInvoiceNumber());

        Manager manager = null;
        if (invoice.getProject() != null && invoice.getProject().getManagerId() != null) {
            manager = managerRepository.findById(invoice.getProject().getManagerId()).orElse(null);
        }

        if (manager == null) {
            throw new IllegalStateException("Manager details not found for branding invoice email");
        }

        String dueDate = invoice.getDueDate() != null ? invoice.getDueDate().format(DateTimeFormatter.ISO_DATE) : "N/A";

        // 2. Dispatch asynchronously
        emailDispatcherService.dispatchInvoices(
                invoice,
                manager,
                request.getRecipients(),
                dueDate,
                pdfBytes,
                filename
        );
    }

    private void assignInvoiceNumber(Invoice invoice) {
        int year = LocalDate.now().getYear();
        ClientInvoiceSequence sequence = sequenceRepository.findByClientIdAndYear(invoice.getClient().getId(), year)
                .orElseGet(() -> ClientInvoiceSequence.builder()
                        .client(invoice.getClient())
                        .year(year)
                        .currentSequence(0)
                        .build());

        int nextSequence = sequence.getCurrentSequence() + 1;
        sequence.setCurrentSequence(nextSequence);
        sequenceRepository.save(sequence);

        invoice.setYear(year);
        invoice.setSequenceNumber(nextSequence);
        invoice.setInvoiceNumber(String.format("%s-%d-%04d", 
                invoice.getClient().getCode(), year, nextSequence));
    }

    private void updateLineItems(Invoice invoice, List<InvoiceLineItemRequest> itemRequests) {
        if (itemRequests == null)
            return;

        invoice.getLineItems().clear();
        for (InvoiceLineItemRequest itemReq : itemRequests) {
            validateLineItemRequest(itemReq);
            invoice.addLineItem(invoiceMapper.toLineItemEntity(itemReq));
        }
        calculationService.recalculateInvoice(invoice);
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

        if (InvoiceStatus.FINAL.equals(invoice.getStatus())) {
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

    @Deprecated
    private void ensureDraftStatus(Invoice invoice) {
        if (!InvoiceStatus.DRAFT.equals(invoice.getStatus())) {
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
