package com.todo.smarttodo.service;

import com.todo.smarttodo.dto.ProductivityTrendDTO;
import com.todo.smarttodo.dto.TaskPriorityAnalyticsDTO;
import com.todo.smarttodo.dto.TaskStatusAnalyticsDTO;
import com.todo.smarttodo.entity.TaskStatus;
import com.todo.smarttodo.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnalyticsService {

    private final TaskRepository taskRepository;

    public AnalyticsService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public TaskStatusAnalyticsDTO getStatusAnalytics(Long userId) {

        TaskStatusAnalyticsDTO dto = new TaskStatusAnalyticsDTO();

        dto.setTotal(taskRepository.countByUserId(userId));
        dto.setCompleted(taskRepository.countByUserIdAndStatus(userId, TaskStatus.COMPLETED));
        dto.setPending(taskRepository.countByUserIdAndStatus(userId, TaskStatus.PENDING));
        dto.setOverdue(taskRepository.countOverdue(userId));

        return dto;
    }

    public List<TaskPriorityAnalyticsDTO> getPriorityAnalytics(Long userId) {

        return taskRepository.countByPriority(userId)
                .stream()
                .map(row -> {
                    TaskPriorityAnalyticsDTO dto = new TaskPriorityAnalyticsDTO();
                    dto.setPriority(row[0].toString());
                    dto.setCount((Long) row[1]);
                    return dto;
                }).toList();
    }

    public List<ProductivityTrendDTO> getWeeklyTrend(Long userId) {

        LocalDateTime startDate = LocalDate.now()
                .minusDays(6)
                .atStartOfDay();

        return taskRepository.completedTrend(userId, startDate)
                .stream()
                .map(row -> {
                    ProductivityTrendDTO dto = new ProductivityTrendDTO();
                    dto.setDate(((java.sql.Date) row[0]).toLocalDate());
                    dto.setCompletedTasks((Long) row[1]);
                    return dto;
                }).toList();
    }
}
