package com.freelance.freelancepm.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeamMemberDTO {
    private Integer id;
    private String name;
    private String role;
    private String initials;
}
