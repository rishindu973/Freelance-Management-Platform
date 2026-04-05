package com.freelance.freelancepm.dto;

import com.freelance.freelancepm.entity.Invoice;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class InvoiceUpdateRequest {

    @Positive(message = "Client ID must be positive")
    private Integer clientId;

    @Positive(message = "Project ID must be positive")
    private Integer projectId;

    private Invoice.Status status;

    private Long version; // For optimistic locking

    @Valid
    private List<InvoiceLineItemRequest> lineItems;
}
