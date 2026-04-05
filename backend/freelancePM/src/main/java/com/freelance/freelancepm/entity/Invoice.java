package com.freelance.freelancepm.entity;

import com.freelance.freelancepm.model.Client;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoice")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    public enum Status {
        DRAFT, FINAL, OVERDUE, PAID, PARTIALLY_PAID, OVERPAID
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Version
    private Long version;

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

    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal total;

    @Column(length = 50)
    private String type;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "invoice_number", length = 50, unique = true, nullable = false)
    private String invoiceNumber;

    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber;

    @Column(nullable = false)
    private int year;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @CreationTimestamp
    @Column(name = "generated_on", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<InvoiceLineItem> lineItems = new ArrayList<>();

    public static final BigDecimal TAX_RATE = new BigDecimal("0.10");

    // Helper methods for bidirectional relationship
    public void addLineItem(InvoiceLineItem lineItem) {
        lineItems.add(lineItem);
        lineItem.setInvoice(this);
    }

    public void removeLineItem(InvoiceLineItem lineItem) {
        lineItems.remove(lineItem);
        lineItem.setInvoice(null);
    }

    public void recalculateTotals() {
        this.subtotal = lineItems.stream()
                .map(InvoiceLineItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.tax = this.subtotal.multiply(TAX_RATE);
        this.total = this.subtotal.add(this.tax);
    }
}