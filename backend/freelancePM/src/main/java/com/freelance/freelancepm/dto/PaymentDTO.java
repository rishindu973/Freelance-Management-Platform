package com.freelance.freelancepm.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDTO {

    private Integer id;
    private Integer invoiceId;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String status;
}