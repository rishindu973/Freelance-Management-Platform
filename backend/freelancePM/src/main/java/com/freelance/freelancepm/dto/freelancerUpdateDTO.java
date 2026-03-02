package com.freelance.freelancepm.dto;

import lombok.Data;

@Data
public class freelancerUpdateDTO {
//    private String fullName;
    private String contactNumber;
    private String title;
    private String driveLink;

    public String getContactNumber() {
        return contactNumber;
    }

    public String getDriveLink() {
        return driveLink;
    }

    public String getTitle() {
        return title;
    }
}
