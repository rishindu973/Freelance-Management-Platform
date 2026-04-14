package com.freelance.freelancepm.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Report {

    public enum ReportType {
        WEEKLY, MONTHLY, ANNUALY
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    private ReportType type;

    @CreationTimestamp
    @Column(name = "generated_on", updatable = false)
    private LocalDateTime generatedOn;

    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal totalProfit;

}
