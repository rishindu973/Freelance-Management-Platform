package com.freelance.freelancepm.dto;

import com.freelance.freelancepm.entity.Activity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ActivityResponse {
    private Long id;
    private Integer managerId;
    private Activity.ActivityType type;
    private String description;
    private LocalDateTime timestamp;
}
