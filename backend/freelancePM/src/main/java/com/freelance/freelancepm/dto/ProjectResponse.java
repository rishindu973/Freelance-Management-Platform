package com.freelance.freelancepm.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class ProjectResponse {
    private Integer id;
    private Integer clientId;
    private Integer managerId;
    private String name;
    private String description;
    private String type;
    private LocalDate startDate;
    private LocalDate deadline;
    private String status;
    private BigDecimal budget;
    private Integer progressPercentage;
    private java.util.List<TeamMemberDTO> team;
}