package com.freelance.freelancepm.dto;

import com.freelance.freelancepm.entity.Invoice;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class InvoiceCreateRequest {

    @NotNull(message = "Client ID is required")
    private Integer clientId;

    private Integer projectId;

    private Invoice.Status status;

    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal total;

    @NotEmpty(message = "At least one line item is required")
    @Valid
    private List<InvoiceLineItemRequest> lineItems;
}
