package com.freelance.freelancepm.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TaskRequest {
    private Integer freelancerId;
    private String title;
    private String description;
    private Integer projectId;
    private LocalDate deadline;
    private String priority;
}
