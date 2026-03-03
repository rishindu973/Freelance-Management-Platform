package com.freelance.freelancepm.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "manager")
@Data
public class Manager {
    @Id
    @Column(name = "user_id")
    private Integer id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "full_name", length = 255, nullable = false)
    private String fullName;
    @Column(name = "company_name", length = 255)
    private String companyName;
    @Column(name = "contact_number", length = 20)
    private String contactNumber;

}
