package com.freelance.freelancepm.entity;

import com.freelance.freelancepm.model.Client;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    public enum Status {
        DRAFT, FINAL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Builder.Default
    private Status status = Status.DRAFT;

    @Column(precision = 15, scale = 2)
    private BigDecimal subtotal;

    @Column(precision = 15, scale = 2)
    private BigDecimal tax;

    @Column(precision = 15, scale = 2)
    private BigDecimal total;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<InvoiceLineItem> lineItems = new ArrayList<>();

    // Helper methods for bidirectional relationship
    public void addLineItem(InvoiceLineItem lineItem) {
        lineItems.add(lineItem);
        lineItem.setInvoice(this);
    }

    public void removeLineItem(InvoiceLineItem lineItem) {
        lineItems.remove(lineItem);
        lineItem.setInvoice(null);
    }
}
