package com.freelance.freelancepm.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ProjectUpdateRequest {
    private Long clientId;
    private String name;
    private String description;
    private String type;
    private LocalDate startDate;
    private LocalDate deadline;
    private String status;
}