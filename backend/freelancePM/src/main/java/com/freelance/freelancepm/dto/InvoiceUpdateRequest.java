package com.freelance.freelancepm.dto;

import com.freelance.freelancepm.entity.Invoice;
import jakarta.validation.Valid;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class InvoiceUpdateRequest {

    private Integer clientId;
    
    private Integer projectId;

    private Invoice.Status status;

    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal total;

    @Valid
    private List<InvoiceLineItemRequest> lineItems;
}
