package com.todo.smarttodo.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ProductivityTrendDTO {
    private LocalDate date;
    private long completedTasks;
}
