package com.todo.smarttodo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
@Entity
@Getter
@Setter
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;
    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;


    private LocalDateTime deadline;


    private LocalDateTime recurrenceEndDate;

    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.PENDING;
    private boolean deleted = false;     // soft delete flag
    private LocalDateTime deletedAt;     // when moved to trash


    @Enumerated(EnumType.STRING)
    private Priority priority;
    @Column(nullable = false)
    private boolean autoPriorityEnabled = true; // true = auto, false = manual

    // Many tasks belong to one user (Many-to-One relationship)
    // user_id column stores the foreign key linking Task to User
    // Hibernate sees the @ManyToOne user field and knows it references another entity.
    // It automatically stores the user's primary key (ID) in the user_id column as a foreign key.
    private boolean reminded5Days;
    private boolean reminded3Days;
    private boolean reminded1Day;
    private boolean reminded10Hours;
    private boolean reminded5Hours;
    private boolean reminded1Hour;
    private boolean overdueAlertSent = false;


    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    // ================= RECURRENCE =================

    private boolean recurring; // true = recurrence enabled

    @Enumerated(EnumType.STRING)
    private RepeatIntervel repeatInterval; // DAILY/WEEKLY/MONTHLY

    private boolean recurrenceActive; // pause / resume

    private boolean recurrenceStopped;
    private boolean nextRecurringGenerated = false;
// permanent stop

     // nullable → infinite
}
