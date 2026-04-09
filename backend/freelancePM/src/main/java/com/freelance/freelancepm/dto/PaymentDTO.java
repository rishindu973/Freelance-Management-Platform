package com.freelance.freelancepm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentDTO {
    private Integer id;
    private Integer invoiceId;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String status;
}

