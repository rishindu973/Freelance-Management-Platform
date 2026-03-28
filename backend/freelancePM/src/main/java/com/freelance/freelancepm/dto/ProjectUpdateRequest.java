package com.freelance.freelancepm.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProjectUpdateRequest {
    private Integer clientId;
    private String name;
    private String description;
    private String type;
    private LocalDate startDate;
    private LocalDate deadline;
    private String status;
    private BigDecimal budget;
}