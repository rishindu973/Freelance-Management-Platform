package com.freelance.freelancepm.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentCreateRequest {
    @NotNull(message = "Payment amount is required")
    @Positive(message = "Payment amount must be positive")
    private BigDecimal amount;

    private String paymentMethod;
    private String referenceNumber;
    private LocalDateTime paymentDate;
}
