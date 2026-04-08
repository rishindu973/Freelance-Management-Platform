package com.freelance.freelancepm.service;

import com.freelance.freelancepm.dto.InvoiceCreateRequest;
import com.freelance.freelancepm.dto.InvoiceResponse;
import com.freelance.freelancepm.dto.InvoiceUpdateRequest;
import com.freelance.freelancepm.dto.SendInvoiceRequest;

import java.util.List;

public interface IInvoiceService {
    InvoiceResponse create(InvoiceCreateRequest req);

    InvoiceResponse update(Integer invoiceId, InvoiceUpdateRequest req);

    InvoiceResponse getById(Integer invoiceId);

    List<InvoiceResponse> list();

    void sendInvoice(Integer invoiceId, SendInvoiceRequest request);
}
