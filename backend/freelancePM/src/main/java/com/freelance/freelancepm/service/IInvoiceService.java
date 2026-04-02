package com.freelance.freelancepm.service;
 
import com.freelance.freelancepm.dto.InvoiceCreateRequest;
import com.freelance.freelancepm.dto.InvoiceResponse;
import com.freelance.freelancepm.dto.InvoiceUpdateRequest;
 
import java.util.List;
 
public interface IInvoiceService {
    InvoiceResponse create(InvoiceCreateRequest req);
    InvoiceResponse update(Long invoiceId, InvoiceUpdateRequest req);
    InvoiceResponse getById(Long invoiceId);
    List<InvoiceResponse> list();
}
