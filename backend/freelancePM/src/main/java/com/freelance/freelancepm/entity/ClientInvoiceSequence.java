package com.freelance.freelancepm.entity;

import com.freelance.freelancepm.model.Client;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "client_invoice_sequence", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"client_id", "year"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientInvoiceSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(nullable = false)
    private int year;

    @Column(name = "current_sequence", nullable = false)
    @Builder.Default
    private int currentSequence = 0;
}
