package com.baeldung.jiralite.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "sprints")
public class Sprint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private String name;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SprintStatus status = SprintStatus.PLANNED;

    public Sprint() {}

    public Sprint(Project project, String name, LocalDate startDate, LocalDate endDate) {
        this.project = project;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getId() { return id; }
    public Project getProject() { return project; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public SprintStatus getStatus() { return status; }
    public void setStatus(SprintStatus status) { this.status = status; }
}
