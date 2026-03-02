package com.freelance.freelancepm.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name="users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name="email", length = 255, unique = true, nullable = false)
    private String email;
    @Column(name = "password", length = 255,nullable = false)
    private String password;
    @Column(name="role",length = 20, nullable = false)
    private String role;

}
