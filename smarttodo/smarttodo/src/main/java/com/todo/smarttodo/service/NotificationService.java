package com.todo.smarttodo.service;

import com.todo.smarttodo.entity.Task;
import com.todo.smarttodo.entity.TaskStatus;
import com.todo.smarttodo.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NotificationService {
    private final TaskRepository taskRepository;
    private final EmailService emailService;

    public NotificationService(TaskRepository taskRepository,
                               EmailService emailService) {
        this.taskRepository = taskRepository;
        this.emailService = emailService;
    }

    private String formatDeadline(LocalDateTime deadline) {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
        return deadline.format(formatter);
    }

    // ================= DEADLINE REMINDERS =================
    public void sendDeadlineReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<Task> tasks = taskRepository.findByStatus(TaskStatus.PENDING);

        for (Task task : tasks) {
            if (task.getDeadline() == null) continue;
            if (task.getDeadline().isBefore(now)) continue;

            Duration duration = Duration.between(now, task.getDeadline());
            long daysLeft = duration.toDays();
            long hoursLeft = duration.toHours();

            if (hoursLeft <= 1 && !task.isReminded1Hour()) {
                sendReminderEmail(task, "⏰ Deadline Reminder: 1 HOUR LEFT");
                task.setReminded1Hour(true);
            } else if (hoursLeft <= 5 && !task.isReminded5Hours()) {
                sendReminderEmail(task, "⏰ Deadline Reminder: 5 HOURS LEFT");
                task.setReminded5Hours(true);
            } else if (hoursLeft <= 10 && !task.isReminded10Hours()) {
                sendReminderEmail(task, "⏰ Deadline Reminder: 10 HOURS LEFT");
                task.setReminded10Hours(true);
            } else if (daysLeft <= 1 && !task.isReminded1Day()) {
                sendReminderEmail(task, "⏰ Deadline Reminder: 1 DAY LEFT");
                task.setReminded1Day(true);
            } else if (daysLeft <= 3 && !task.isReminded3Days()) {
                sendReminderEmail(task, "⏰ Deadline Reminder: 3 DAYS LEFT");
                task.setReminded3Days(true);
            } else if (daysLeft <= 5 && !task.isReminded5Days()) {
                sendReminderEmail(task, "⏰ Deadline Reminder: 5 DAYS LEFT");
                task.setReminded5Days(true);
            }
        }

        taskRepository.saveAll(tasks);
    }

    // ================= OVERDUE ALERT =================
    public void sendOverdueAlerts() {
        LocalDateTime now = LocalDateTime.now();
        List<Task> tasks = taskRepository.findOverdueTasks(now);

        for (Task task : tasks) {
            if (task.isOverdueAlertSent()) continue;

            // ✅ Mark overdue for all tasks
            task.setStatus(TaskStatus.OVERDUE);
            sendOverdueEmail(task);
            task.setOverdueAlertSent(true);
        }

        taskRepository.saveAll(tasks);
    }

    // ================= PRIORITY BADGE =================
    private String priorityBadge(Task task) {
        return switch (task.getPriority()) {
            case CRITICAL ->
                    "<span style='color:white;background:#d32f2f;padding:6px 10px;border-radius:6px;'>CRITICAL</span>";
            case HIGH ->
                    "<span style='color:white;background:#f57c00;padding:6px 10px;border-radius:6px;'>HIGH</span>";
            case MEDIUM ->
                    "<span style='color:white;background:#1976d2;padding:6px 10px;border-radius:6px;'>MEDIUM</span>";
            case LOW ->
                    "<span style='color:white;background:#388e3c;padding:6px 10px;border-radius:6px;'>LOW</span>";
        };
    }

    // ================= REMINDER EMAIL =================
    private void sendReminderEmail(Task task, String subject) {
        String snoozeLink = "http://localhost:8080/task-action/snooze?taskId=" + task.getId();
        String postponeLink = "http://localhost:8080/task-action/postpone?taskId=" + task.getId();

        String body = """
                <h2>%s</h2>
                <p>Hi <b>%s</b>,</p>
                <p>Your task <b>%s</b> is approaching its deadline.</p>
                <p><b>Deadline:</b> %s</p>
                <p><b>Priority:</b> %s</p>
                <a href="%s">Snooze 1 Day</a> |
                <a href="%s">Postpone 3 Days</a>
                """.formatted(
                subject,
                task.getUser().getName(),
                task.getTitle(),
                formatDeadline(task.getDeadline()),
                priorityBadge(task),
                snoozeLink,
                postponeLink
        );

        emailService.sendEmail(task.getUser().getEmail(), subject, body);
    }

    // ================= OVERDUE EMAIL =================
    private void sendOverdueEmail(Task task) {
        String snoozeLink = "http://localhost:8080/task-action/snooze?taskId=" + task.getId();
        String postponeLink = "http://localhost:8080/task-action/postpone?taskId=" + task.getId();

        String body = """
                <div style="font-family: Arial, sans-serif; max-width:600px; margin:auto;
                            border:1px solid #f44336; padding:20px; border-radius:10px;
                            background:#fff5f5;">
                    <h2 style="color:#d32f2f;">❌ Task Overdue</h2>
                    <p>Hi <b>%s</b>,</p>
                    <p>Your task <b>“%s”</b> has <b>missed its deadline</b>.</p>
                    <p>
                        <b>Deadline was:</b> %s <br/>
                        <b>Priority:</b> %s
                    </p>
                    <hr/>
                    <p>Please take action immediately:</p>
                    <a href="%s"
                       style="display:inline-block;padding:12px 18px;
                              background:#1976d2;color:white;
                              text-decoration:none;border-radius:6px;
                              margin-right:10px;">
                       Snooze 1 Day
                    </a>
                    <a href="%s"
                       style="display:inline-block;padding:12px 18px;
                              background:#555;color:white;
                              text-decoration:none;border-radius:6px;">
                       Postpone 3 Days
                    </a>
                    <br/><br/>
                    <p style="font-size:12px;color:#777;">
                        This overdue alert was sent automatically by SmartTodo.
                    </p>
                </div>
                """.formatted(
                task.getUser().getName(),
                task.getTitle(),
                formatDeadline(task.getDeadline()),
                priorityBadge(task),
                snoozeLink,
                postponeLink
        );

        emailService.sendEmail(task.getUser().getEmail(),
                "❌ Task Overdue – Immediate Action Required", body);
    }

    // ================= TASK CREATED EMAIL =================
    public void sendTaskCreatedEmail(Task task, boolean isAutoGenerated) {
        String subject = "✅ New Task Created: " + task.getTitle();

        String autoNote = "";
        if (isAutoGenerated) {
            autoNote = "<p><i>This task was auto-generated.</i></p>";
        }

        String body = """
                <h2>%s</h2>
                <p>Hi <b>%s</b>,</p>
                %s
                <p><b>Title:</b> %s</p>
                <p><b>Deadline:</b> %s</p>
                <p><b>Priority:</b> %s</p>
                """.formatted(
                subject,
                task.getUser().getName(),
                autoNote,
                task.getTitle(),
                formatDeadline(task.getDeadline()),
                priorityBadge(task)
        );

        emailService.sendEmail(task.getUser().getEmail(), subject, body);
    }
}
