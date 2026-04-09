package com.freelance.freelancepm.mapper;

import com.freelance.freelancepm.dto.InvoiceLineItemRequest;
import com.freelance.freelancepm.dto.InvoiceLineItemResponse;
import com.freelance.freelancepm.dto.InvoiceResponse;
import com.freelance.freelancepm.entity.Invoice;
import com.freelance.freelancepm.entity.InvoiceLineItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.stream.Collectors;

@Component
public class InvoiceMapper {

    public InvoiceResponse toResponse(Invoice invoice) {
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
        response.setVersion(invoice.getVersion());
        response.setInvoiceNumber(invoice.getInvoiceNumber());
        response.setClientName(invoice.getClient().getName());
        response.setFailureReason(invoice.getFailureReason());
        response.setLastSentAt(invoice.getLastSentAt());

        if (invoice.getLineItems() != null) {
            response.setLineItems(invoice.getLineItems().stream()
                    .map(this::toLineItemResponse)
                    .collect(Collectors.toList()));
        }

        return response;
    }

    public InvoiceLineItemResponse toLineItemResponse(InvoiceLineItem item) {
        InvoiceLineItemResponse res = new InvoiceLineItemResponse();
        res.setId(item.getId());
        res.setDescription(item.getDescription());
        res.setQuantity(item.getQuantity());
        res.setUnitPrice(item.getUnitPrice());
        res.setAmount(item.getAmount());
        return res;
    }

    public InvoiceLineItem toLineItemEntity(InvoiceLineItemRequest req) {
        BigDecimal amount = req.getUnitPrice().multiply(new BigDecimal(req.getQuantity()));
        return InvoiceLineItem.builder()
                .description(req.getDescription())
                .quantity(req.getQuantity())
                .unitPrice(req.getUnitPrice())
                .amount(amount)
                .build();
    }
}
