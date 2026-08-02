package com.freelance.freelancepm.mapper;

import com.freelance.freelancepm.dto.InvoiceLineItemRequest;
import com.freelance.freelancepm.dto.InvoiceLineItemResponse;
import com.freelance.freelancepm.dto.InvoiceListDTO;
import com.freelance.freelancepm.dto.InvoiceResponse;
import com.freelance.freelancepm.entity.Invoice;
import com.freelance.freelancepm.entity.InvoiceLineItem;
import com.freelance.freelancepm.entity.Manager;
import com.freelance.freelancepm.repository.ManagerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class InvoiceMapper {

        private final ManagerRepository managerRepository;

        public InvoiceListDTO toListDTO(Invoice invoice) {
                return InvoiceListDTO.builder()
                                .id(invoice.getId())
                                .invoiceNumber(invoice.getInvoiceNumber())
                                .clientId(invoice.getClient() != null ? invoice.getClient().getId() : null)
                                .clientName(invoice.getClient() != null ? invoice.getClient().getName() : null)
                                .createdAt(invoice.getCreatedAt())
                                .total(invoice.getTotal())
                                .status(invoice.getStatus())
                                .displayStatus(invoice.getStatus() != null
                                                ? invoice.getStatus().getDisplayStatus()
                                                : null)
                                .build();
        }

        public InvoiceResponse toResponse(Invoice invoice) {
                InvoiceResponse response = new InvoiceResponse();
                response.setId(invoice.getId());
                response.setDescription(invoice.getDescription());

                // Client
                if (invoice.getClient() != null) {
                        response.setClientId(invoice.getClient().getId());
                        response.setClientName(invoice.getClient().getName());
                        response.setClientAddress(invoice.getClient().getAddress());
                        response.setClientEmail(invoice.getClient().getEmail());
                        response.setClientPhone(invoice.getClient().getPhone());
                }

                // Projects
                if (invoice.getProjects() != null && !invoice.getProjects().isEmpty()) {
                        response.setProjectIds(invoice.getProjects().stream()
                                        .map(p -> p.getId())
                                        .collect(Collectors.toList()));
                        response.setProjectNames(invoice.getProjects().stream()
                                        .map(p -> p.getName())
                                        .collect(Collectors.toList()));
                }

                // Manager / Company branding
                Manager manager = resolveManager(invoice);
                if (manager != null) {
                        response.setCompanyName(manager.getCompanyName());
                        response.setCompanyAddress(manager.getAddress());
                        response.setCompanyPhone(manager.getContactNumber());
                        response.setLogoUrl(manager.getLogoUrl());
                        if (manager.getUser() != null) {
                                response.setCompanyEmail(manager.getUser().getEmail());
                        }
                }

                response.setStatus(invoice.getStatus());
                response.setDisplayStatus(invoice.getStatus() != null
                                ? invoice.getStatus().getDisplayStatus()
                                : null);
                response.setSubtotal(invoice.getSubtotal());
                response.setTax(invoice.getTax());
                response.setTotal(invoice.getTotal());
                response.setDueDate(invoice.getDueDate());
                response.setCreatedAt(invoice.getCreatedAt());
                response.setUpdatedAt(invoice.getUpdatedAt());
                response.setVersion(invoice.getVersion());
                response.setInvoiceNumber(invoice.getInvoiceNumber());
                response.setFailureReason(invoice.getFailureReason());
                response.setLastSentAt(invoice.getLastSentAt());

                if (invoice.getLineItems() != null) {
                        response.setLineItems(invoice.getLineItems().stream()
                                        .map(this::toLineItemResponse)
                                        .collect(Collectors.toList()));
                }

                return response;
        }

        public InvoiceLineItemResponse toLineItemResponse(InvoiceLineItem item) {
                InvoiceLineItemResponse res = new InvoiceLineItemResponse();
                res.setId(item.getId());
                res.setDescription(item.getDescription());
                res.setQuantity(item.getQuantity());
                res.setUnitPrice(item.getUnitPrice());
                res.setAmount(item.getAmount());
                return res;
        }

        public InvoiceLineItem toLineItemEntity(InvoiceLineItemRequest req) {
                BigDecimal amount = req.getUnitPrice().multiply(new BigDecimal(req.getQuantity()));
                return InvoiceLineItem.builder()
                                .description(req.getDescription())
                                .quantity(req.getQuantity())
                                .unitPrice(req.getUnitPrice())
                                .amount(amount)
                                .build();
        }

        private Manager resolveManager(Invoice invoice) {
                if (invoice.getProjects() != null && !invoice.getProjects().isEmpty()) {
                        Integer managerId = invoice.getProjects().get(0).getManagerId();
                        if (managerId != null) {
                                return managerRepository.findById(managerId).orElse(null);
                        }
                }
                return null;
        }
}
