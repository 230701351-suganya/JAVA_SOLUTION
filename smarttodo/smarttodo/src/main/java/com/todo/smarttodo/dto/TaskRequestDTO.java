package com.todo.smarttodo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.todo.smarttodo.entity.Priority;
import com.todo.smarttodo.entity.RepeatIntervel;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;


@Getter
@Setter
public class TaskRequestDTO {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @Future(message = "Deadline must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime deadline;

    private Priority priority;
    private boolean recurring;
    private RepeatIntervel repeatInterval;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime recurrenceEndDate;


    // getters and setters
}
