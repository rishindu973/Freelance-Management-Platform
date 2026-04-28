package com.freelance.freelancepm.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FreelancerDTO {
    private String email;
    private String password;
    private String role;

    private String fullName;
    private String title;
    private String contactNumber;
    private BigDecimal salary;
    private String status;
    private String driveLink;
}
