package com.freelance.freelancepm.dto;

import com.freelance.freelancepm.entity.InvoiceStatus;
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
    private InvoiceStatus status;
    private String displayStatus;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal total;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;

    private String invoiceNumber;
    private String description; // Notes field

    // Client details
    private String clientName;
    private String clientAddress;
    private String clientEmail;
    private String clientPhone;

    // Project details (one or more)
    private List<Integer> projectIds;
    private List<String> projectNames;

    // Manager / Company branding details
    private String companyName;
    private String companyEmail;
    private String companyPhone;
    private String companyAddress;
    private String logoUrl;

    private String failureReason;
    private LocalDateTime lastSentAt;
    private List<InvoiceLineItemResponse> lineItems;
}
