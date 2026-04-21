package com.freelance.freelancepm.dto;

import com.freelance.freelancepm.entity.InvoiceStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class InvoiceUpdateRequest {

    @Positive(message = "Client ID must be positive")
    private Integer clientId;

    /**
     * Optional — replaces the full project list when provided and non-empty.
     * Pass null or omit to leave existing projects unchanged.
     */
    private List<Integer> projectIds;

    /**
     * Optional — updates the notes/service description.
     * Pass null or omit to leave existing description unchanged.
     */
    private String description;

    private InvoiceStatus status;

    private Long version; // For optimistic locking

    @Valid
    private List<InvoiceLineItemRequest> lineItems;
}
