package com.freelance.freelancepm.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class InvoicePaidEvent extends ApplicationEvent {
    private final Integer invoiceId;
    private final Integer clientId;
    private final BigDecimal amount;
    private final LocalDateTime paidDate;

    public InvoicePaidEvent(Object source, Integer invoiceId, Integer clientId, BigDecimal amount, LocalDateTime paidDate) {
        super(source);
        this.invoiceId = invoiceId;
        this.clientId = clientId;
        this.amount = amount;
        this.paidDate = paidDate;
    }
}
