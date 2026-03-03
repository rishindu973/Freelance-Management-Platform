package com.freelance.freelancepm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "project")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "client_id")
    private Integer clientId;

    @Column(name = "manager_id", nullable = false)
    private Integer managerId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(length = 100)
    private String type;

    @Column(name = "start_date")
    private LocalDate startDate;

    private LocalDate deadline;

    @Builder.Default
    @Column(length = 50)
    private String status = "pending";

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "project_freelancer", joinColumns = @JoinColumn(name = "project_id"), inverseJoinColumns = @JoinColumn(name = "freelancer_id"))
    @Builder.Default
    private java.util.List<Freelancer> team = new java.util.ArrayList<>();
}