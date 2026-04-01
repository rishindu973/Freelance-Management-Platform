package com.freelance.freelancepm.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InvoiceLineItemResponse {
    
    private Long id;
    private String description;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
}
