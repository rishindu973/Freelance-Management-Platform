package com.freelance.freelancepm.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ProjectResponse {
    private Long id;
    private Long clientId;
    private Long managerId;
    private String name;
    private String description;
    private String type;
    private LocalDate startDate;
    private LocalDate deadline;
    private String status;
}