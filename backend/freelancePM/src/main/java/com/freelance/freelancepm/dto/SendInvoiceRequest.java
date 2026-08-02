package com.freelance.freelancepm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendInvoiceRequest {
    @NotEmpty(message = "Recipient list cannot be empty")
    private List<@Email(message = "Invalid email format") String> recipients;
}
