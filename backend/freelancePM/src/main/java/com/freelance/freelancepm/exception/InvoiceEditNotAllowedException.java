package com.freelance.freelancepm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class InvoiceEditNotAllowedException extends RuntimeException {
    public InvoiceEditNotAllowedException(String message) {
        super(message);
    }
}
