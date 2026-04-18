package com.todo.smarttodo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskPriorityAnalyticsDTO {
    private String priority;
    private long count;
}
