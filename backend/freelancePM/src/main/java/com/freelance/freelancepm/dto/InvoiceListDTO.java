package com.freelance.freelancepm.dto;

import com.freelance.freelancepm.entity.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lightweight DTO for the invoice list endpoint.
 * Exposes only the summary fields needed for tabular/list views,
 * avoiding the overhead of serializing full line-item details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceListDTO {

    private Integer id;
    private String invoiceNumber;
    private String clientName;
    private LocalDateTime issueDate;
    private BigDecimal totalAmount;
    private InvoiceStatus status;
    private String displayStatus;
}
