package com.freelance.freelancepm.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    private Integer id;
    private Integer invoiceId;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private String status;
}
