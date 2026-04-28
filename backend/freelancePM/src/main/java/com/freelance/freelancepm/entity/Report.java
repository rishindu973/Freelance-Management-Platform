package com.freelance.freelancepm.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Report {

    public enum ReportType {
        WEEKLY, MONTHLY, ANNUALLY
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    private ReportType type;

    @CreationTimestamp
    @Column(name = "generated_on", updatable = false)
    private LocalDateTime generatedOn;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReportDetail> details = new ArrayList<>();

    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal totalProfit;

    public void addDetail(ReportDetail detail) {
        details.add(detail);
        detail.setReport(this);
    }

    public void removeDetail(ReportDetail detail) {
        details.remove(detail);
        detail.setReport(null);
    }

    public void syncTotals() {
        this.totalIncome = details.stream()
                .map(ReportDetail::getIncome)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalExpense = details.stream()
                .map(ReportDetail::getExpense)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalProfit = this.totalIncome.subtract(this.totalExpense);
    }
}
