package com.todo.smarttodo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskStatusAnalyticsDTO {
    private long total;
    private long completed;
    private long pending;
    private long overdue;
}
