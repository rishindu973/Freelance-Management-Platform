package com.freelance.freelancepm.dto;

import lombok.Data;

@Data
public class TaskRequest {
    private String freelancerEmail;
    private String title;
    private String description;
    private Integer managerId;
}
