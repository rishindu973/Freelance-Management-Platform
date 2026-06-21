package com.freelance.freelancepm.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TaskDTO {
    private Integer id;
    private String title;
    private String description;
    private String freelancerEmail;
    private String freelancerName;
    private String status;
    private String priority;
    private LocalDate deadline;
    private Integer projectId;
}
