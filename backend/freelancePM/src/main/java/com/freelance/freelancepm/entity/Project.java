package com.freelance.freelancepm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "contract_type_id")
    private ContractType contractType;

    @Column(name = "manager_id", nullable = false)
    private Integer managerId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(length = 100)
    private String type;

    private LocalDate startDate;

    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    private ProjectStatus status = ProjectStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    private ProgressStatus progressStatus = ProgressStatus.NOT_STARTED;

    private Integer progressPercentage = 0;

    private Boolean urgent = false;

    private Boolean overdue = false;

    private Boolean archived = false;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "project_freelancer",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "freelancer_id")
    )
    @Builder.Default
    private List<Freelancer> team = new ArrayList<>();
}