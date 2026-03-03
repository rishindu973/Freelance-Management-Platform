package com.freelance.freelancepm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ProjectCreateRequest {
    @NotNull
    private Integer clientId;
    @NotBlank
    private String name;
    private String description;
    private String type;
    private LocalDate startDate;
    private LocalDate deadline;
}