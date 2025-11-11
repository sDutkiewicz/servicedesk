package com.servicedesk.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "tickets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Ticket {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank private String title;

    @Column(length = 4000)
    private String description;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Priority priority;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Status status;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private User reporter;                // kto zgłosił

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id")
    private Technician technician;        // kto obsługuje (opcjonalnie przy tworzeniu)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;            // typ zgłoszenia

    private LocalDateTime createdAt;
    private LocalDateTime closedAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = Status.OPEN;
        if (priority == null) priority = Priority.MEDIUM;
    }

    public enum Status { OPEN, IN_PROGRESS, RESOLVED, CLOSED }
    public enum Priority { LOW, MEDIUM, HIGH, CRITICAL }
}
