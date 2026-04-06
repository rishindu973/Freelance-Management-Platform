package com.freelance.freelancepm.dto;

import com.freelance.freelancepm.entity.Invoice;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class InvoiceResponse {

    private Integer id;
    private Integer clientId;
    private Integer projectId;
    private Invoice.Status status;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal total;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;
    private BigDecimal remainingBalance;

    private List<InvoiceLineItemResponse> lineItems;
}
