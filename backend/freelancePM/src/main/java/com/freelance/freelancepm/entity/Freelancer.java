package com.freelance.freelancepm.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
@Table(name = "freelancer")
public class Freelancer {
    @Id
    @Column(name = "user_id")
    private Integer id;
    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne
    @JoinColumn(name = "manager_id")
    private User manager;

    @Column(name = "full_name", length = 255, nullable = false)
    private String fullName;
    @Column(name = "title", length = 255)
    private String title;
    @Column(name = "contact_number", length = 20)
    private String contactNumber;
    @Column(name = "salary", precision = 10, scale = 2)
    private BigDecimal salary;
    @Column(name = "status", length = 50)
    private String status;
    @Column(name = "drive_link", columnDefinition = "TEXT")
    private String driveLink;
}
