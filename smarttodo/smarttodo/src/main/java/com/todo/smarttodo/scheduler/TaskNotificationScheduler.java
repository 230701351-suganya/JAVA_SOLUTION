package com.todo.smarttodo.scheduler;

import com.todo.smarttodo.entity.RecurrenceMode;
import com.todo.smarttodo.entity.Task;
import com.todo.smarttodo.entity.TaskStatus;
import com.todo.smarttodo.repository.TaskRepository;
import com.todo.smarttodo.service.EmailService;
import com.todo.smarttodo.service.NotificationService;
import com.todo.smarttodo.service.TaskService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class TaskNotificationScheduler {


    private final NotificationService notificationService;
    private final TaskRepository taskRepository;
    private final TaskService taskService;

    public TaskNotificationScheduler(NotificationService notificationService,
                                     TaskRepository taskRepository,
                                     TaskService taskService) {
        this.notificationService = notificationService;
        this.taskRepository = taskRepository;
        this.taskService = taskService;
    }
    // ================= FORMAT DATE =================


    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void runNotifications() {
        System.out.println("🔥 Scheduler running at " + LocalDateTime.now());
        try {
            notificationService.sendDeadlineReminders();
            notificationService.sendOverdueAlerts();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Scheduled(cron = "0 0 2 * * *") // every day 2 AM
    //@Scheduled(cron = "0 */1 * * * *") // every 1 minute
    @Transactional
    public void purgeDeletedTasks() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        taskRepository.deleteByDeletedTrueAndDeletedAtBefore(cutoff);
    }








}
