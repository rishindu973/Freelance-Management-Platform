package com.freelance.freelancepm.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InvoiceLineItemRequest {

    private Long id; // Optional, present when updating existing line item

    @NotBlank(message = "Description cannot be empty")
    @Size(max = 255, message = "Description too long")
    private String description;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    @Max(value = 999999, message = "Quantity exceeds limit")
    private Integer quantity;

    @NotNull(message = "Unit price is required")
    @Positive(message = "Unit price must be positive")
    @Max(value = 9999999, message = "Unit price exceeds limit")
    private BigDecimal unitPrice;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;
}
