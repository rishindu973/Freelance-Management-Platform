package com.freelance.freelancepm.service;

import com.freelance.freelancepm.dto.InvoiceCreateRequest;
import com.freelance.freelancepm.dto.InvoiceListDTO;
import com.freelance.freelancepm.dto.InvoiceResponse;
import com.freelance.freelancepm.dto.InvoiceUpdateRequest;
import com.freelance.freelancepm.dto.SendInvoiceRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface IInvoiceService {
    InvoiceResponse create(InvoiceCreateRequest req);

    InvoiceResponse update(Integer invoiceId, InvoiceUpdateRequest req);

    InvoiceResponse getById(Integer invoiceId);

    List<InvoiceResponse> list();

    /**
     * Returns all invoices matching the optional filters.
     *
     * @param clientId  filter by client (nullable — no filter if null)
     * @param startDate filter invoices created on or after this date (nullable)
     * @param endDate   filter invoices created on or before this date (nullable)
     * @param pageable  pagination and sorting options
     * @return matching invoices as paginated lightweight list DTOs
     */
    Page<InvoiceListDTO> listAll(Integer clientId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    void sendInvoice(Integer invoiceId, SendInvoiceRequest request);

    InvoiceResponse updateStatus(Integer invoiceId, com.freelance.freelancepm.dto.InvoiceStatusUpdateRequest req);

    void addPayment(Integer invoiceId, com.freelance.freelancepm.dto.PaymentCreateRequest req);
}
