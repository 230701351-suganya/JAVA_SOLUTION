package com.todo.smarttodo.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskResponseDTO {

    private Long id;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private String status;
    private String priority;
    private Long userId;
    private boolean recurring;
    private boolean recurrenceActive;
    private boolean recurrenceStopped;
    private String repeatInterval;
    private LocalDateTime recurrenceEndDate;


    // getters and setters
}
