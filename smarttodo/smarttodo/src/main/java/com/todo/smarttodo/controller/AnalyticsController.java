package com.todo.smarttodo.controller;

import com.todo.smarttodo.dto.ProductivityTrendDTO;
import com.todo.smarttodo.dto.TaskPriorityAnalyticsDTO;
import com.todo.smarttodo.dto.TaskStatusAnalyticsDTO;
import com.todo.smarttodo.service.AnalyticsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.todo.smarttodo.config.security.SecurityUtil;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@PreAuthorize("hasRole('USER')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final SecurityUtil securityUtil;

    public AnalyticsController(AnalyticsService analyticsService,SecurityUtil securityUtil) {
        this.analyticsService = analyticsService;
        this.securityUtil=securityUtil;

    }

    @GetMapping("/status")
    public TaskStatusAnalyticsDTO statusAnalytics() {
        Long userId = securityUtil.getCurrentUserId();
        return analyticsService.getStatusAnalytics(userId);
    }

    @GetMapping("/priority")
    public List<TaskPriorityAnalyticsDTO> priorityAnalytics() {
        Long userId = securityUtil.getCurrentUserId();
        return analyticsService.getPriorityAnalytics(userId);
    }

    @GetMapping("/trend/weekly")
    public List<ProductivityTrendDTO> weeklyTrend() {
        Long userId = securityUtil.getCurrentUserId();
        return analyticsService.getWeeklyTrend(userId);
    }
}
