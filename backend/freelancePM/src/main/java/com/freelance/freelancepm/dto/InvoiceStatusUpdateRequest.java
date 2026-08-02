package com.freelance.freelancepm.dto;

import com.freelance.freelancepm.entity.InvoiceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InvoiceStatusUpdateRequest {
    @NotNull(message = "Target status is required")
    private InvoiceStatus targetStatus;
    
    // Optional amount if updating to PAID manually via a modal that tracks payment
    private java.math.BigDecimal amount;
}
