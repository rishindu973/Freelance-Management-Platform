package com.freelance.freelancepm.dto;

import com.freelance.freelancepm.entity.Invoice;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class InvoiceCreateRequest {

    @NotNull(message = "Client ID is required")
    @Positive(message = "Client ID must be positive")
    private Integer clientId;

    @Positive(message = "Project ID must be positive")
    private Integer projectId;

    private Invoice.Status status;

    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal total;

    @NotEmpty(message = "At least one line item is required")
    @Valid
    private List<InvoiceLineItemRequest> lineItems;
}
