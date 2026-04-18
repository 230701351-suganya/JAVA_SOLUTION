package com.todo.smarttodo.service;

import com.todo.smarttodo.controller.TaskActionController;
import com.todo.smarttodo.entity.*;
import com.todo.smarttodo.exception.InvalidTaskOperationException;
import com.todo.smarttodo.exception.ResourceNotFoundException;
import com.todo.smarttodo.repository.TaskRepository;
import com.todo.smarttodo.repository.UserRepository;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.todo.smarttodo.dto.TaskRequestDTO;
import com.todo.smarttodo.dto.TaskResponseDTO;
import com.todo.smarttodo.config.security.SecurityUtil;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.todo.smarttodo.entity.RepeatIntervel.MONTHLY;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final SecurityUtil securityUtil;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository, NotificationService notificationService,SecurityUtil securityUtil) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.securityUtil=securityUtil;
    }

    // Create a new task for a user
    //public Task createTask(Long userId, String title, String description, LocalDateTime deadline) {
    //User user = userRepository.findById(userId)
    //.orElseThrow(() -> new RuntimeException("User not found"));

    //Task task = new Task();
    //task.setTitle(title);
    //task.setDescription(description);
    //task.setDeadline(deadline);
    //task.setUser(user);

    // Default status and priority
    //task.setStatus(TaskStatus.PENDING);
    //task.setPriority(Priority.LOW);

    //return taskRepository.save(task);
    //}
    public TaskResponseDTO createTask(TaskRequestDTO dto) {

        // 1️⃣ Get logged-in user's email from JWT
        String email = securityUtil.getCurrentUserEmail();

        // 2️⃣ Fetch User from DB
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 3️⃣ Create task
        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setDeadline(dto.getDeadline());
        task.setUser(user);

        task.setStatus(TaskStatus.PENDING);
        if (dto.getPriority() != null) {
            task.setPriority(dto.getPriority());
            task.setAutoPriorityEnabled(false); // manual priority disables auto
        } else if (task.isAutoPriorityEnabled()) {
            task.setPriority(calculatePriority(task));
        }
        // ===== RECURRENCE SETUP =====
        if (dto.isRecurring()) {

            if (dto.getRepeatInterval() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Repeat interval required for recurring task");

            }

            task.setRecurring(true);
            task.setRepeatInterval(dto.getRepeatInterval());
            task.setRecurrenceActive(true);
            task.setRecurrenceStopped(false);
            task.setRecurrenceEndDate(dto.getRecurrenceEndDate());

        } else {

            task.setRecurring(false);
            task.setRepeatInterval(null);
            task.setRecurrenceActive(false);
            task.setRecurrenceStopped(false);
            task.setRecurrenceEndDate(null);

        }


        Task savedTask = taskRepository.save(task);
        notificationService.sendTaskCreatedEmail(savedTask, false);

        return mapToResponseDTO(savedTask);
    }

    //}
    public Page<TaskResponseDTO> getTasksByUser(int page, int size) {

        // 1️⃣ Logged-in user
        String email = securityUtil.getCurrentUserEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 2️⃣ Pagination
        Pageable pageable = PageRequest.of(page, size);

        // 3️⃣ Fetch only THIS user's tasks
        Page<Task> taskPage = taskRepository.findByUserIdAndDeletedFalse(user.getId(), pageable);

        return taskPage.map(this::mapToResponseDTO);
    }


    // Update task status
    //public Task updateTaskStatus(Long taskId, TaskStatus status) {
    //Task task = taskRepository.findById(taskId)
    //.orElseThrow(() -> new RuntimeException("Task not found"));
    //task.setStatus(status);
    //return taskRepository.save(task);
    //}
    @Transactional
    public TaskResponseDTO updateTaskStatus(Long taskId, TaskStatus status) {

        User user = userRepository.findByEmail(securityUtil.getCurrentUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (!task.getUser().getId().equals(user.getId()))
            throw new AccessDeniedException("Not allowed");

        validateStatusTransition(task.getStatus(), status);

        task.setStatus(status);
        Task savedTask = taskRepository.save(task); // save first

        if (status == TaskStatus.COMPLETED
                && savedTask.isRecurring()
                && savedTask.isRecurrenceActive()
                && !savedTask.isRecurrenceStopped()) {
            task.setCompletedAt(LocalDateTime.now());

            generateNextRecurringTask(savedTask); // use saved task
        }

        return mapToResponseDTO(savedTask);
    }





    public TaskResponseDTO updateTask(Long taskId, TaskRequestDTO dto) {
        // 1️⃣ Get logged-in user
        String email = securityUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 2️⃣ Fetch task
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        // 3️⃣ Ownership check
        if (!task.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not allowed to update this task");
        }
        // 🔒 4️⃣ HARD BLOCK — COMPLETED TASK
        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Completed task cannot be edited"
            );
        }

        // 4️⃣ Update fields if they are not null
        if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
            task.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null && !dto.getDescription().isBlank()) {
            task.setDescription(dto.getDescription());
        }
        if (dto.getDeadline() != null) {
            task.setDeadline(dto.getDeadline());
        }
        if (dto.getPriority() != null) {
            task.setPriority(dto.getPriority());
            task.setAutoPriorityEnabled(false);
        }

        // 5️⃣ Recalculate priority whenever deadline changes
        // Recalculate priority only if autoPriorityEnabled is true
        if (task.isAutoPriorityEnabled()) {
            task.setPriority(calculatePriority(task));
        }
        // ===== RECURRENCE UPDATE =====
        if (dto.isRecurring()) {

            if (!task.isRecurring()) {
                // normal → recurring
                if (dto.getRepeatInterval() == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Repeat interval required for recurring task");
                }

                task.setRecurring(true);
                task.setRepeatInterval(dto.getRepeatInterval());
                task.setRecurrenceActive(true);
                task.setRecurrenceStopped(false);
                task.setRecurrenceEndDate(dto.getRecurrenceEndDate());

            } else {
                // recurring → recurring (update config)
                if (dto.getRepeatInterval() != null) {
                    task.setRepeatInterval(dto.getRepeatInterval());
                }
                task.setRecurrenceEndDate(dto.getRecurrenceEndDate());
            }

        } else {
            // recurring → normal
            task.setRecurring(false);
            task.setRepeatInterval(null);
            task.setRecurrenceActive(false);
            task.setRecurrenceStopped(false);
            task.setRecurrenceEndDate(null);
        }





        // 6️⃣ Save and return DTO
        Task updatedTask = taskRepository.save(task);
        return mapToResponseDTO(updatedTask);
    }
    private void generateNextRecurringTask(Task completedTask) {
        if (!completedTask.isRecurring()) return;
        if (!completedTask.isRecurrenceActive() || completedTask.isRecurrenceStopped()) return;
//      🛑 Prevent duplicate generation
        if (completedTask.isNextRecurringGenerated()) return;
        // Calculate next deadline
        LocalDateTime nextDeadline = calculateNextDeadline(
                completedTask.getDeadline(),
                completedTask.getRepeatInterval()
        );

        // Stop recurrence if end date exceeded
        if (completedTask.getRecurrenceEndDate() != null &&
                nextDeadline.isAfter(completedTask.getRecurrenceEndDate())) {

            completedTask.setRecurrenceStopped(true);
            taskRepository.save(completedTask);
            return;
        }

        // Create next task based on the latest saved task properties
        Task nextTask = new Task();
        nextTask.setTitle(completedTask.getTitle());
        nextTask.setDescription(completedTask.getDescription());
        nextTask.setUser(completedTask.getUser());
        nextTask.setDeadline(nextDeadline);

        // Copy priority and auto-priority
        nextTask.setAutoPriorityEnabled(completedTask.isAutoPriorityEnabled());
        if (nextTask.isAutoPriorityEnabled()) {
            nextTask.setPriority(calculatePriority(nextTask));
        } else {
            nextTask.setPriority(completedTask.getPriority());
        }

        // Reset reminders
        resetReminders(nextTask);

        // Recurrence properties
        nextTask.setRecurring(true);
        nextTask.setRepeatInterval(completedTask.getRepeatInterval());
        nextTask.setRecurrenceActive(completedTask.isRecurrenceActive());
        nextTask.setRecurrenceStopped(false);
        nextTask.setRecurrenceEndDate(completedTask.getRecurrenceEndDate());
        nextTask.setNextRecurringGenerated(false);
        // Save and send notification
        Task savedNextTask = taskRepository.save(nextTask);
        completedTask.setNextRecurringGenerated(true);
        taskRepository.save(completedTask);
        notificationService.sendTaskCreatedEmail(savedNextTask, true); // true = recurrence
    }


    private LocalDateTime calculateNextDeadline(
            LocalDateTime deadline,
            RepeatIntervel interval) {

        return switch (interval) {
            case DAILY -> deadline.plusDays(1);
            case WEEKLY -> deadline.plusWeeks(1);
            case MONTHLY -> deadline.plusMonths(1);
        };
    }




    public void deleteTask(Long taskId) {
        // 1️⃣ Fetch the task
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        // 2️⃣ Get the currently logged-in user from JWT
        String email = securityUtil.getCurrentUserEmail();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 3️⃣ Check ownership
        if (!task.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not allowed to delete this task");
        }

        // 4️⃣ Delete the task
        taskRepository.delete(task);
    }
    @Transactional
    public void emptyTrash() {
        User user = getLoggedInUser();
        taskRepository.deleteByUserIdAndDeletedTrue(user.getId());
    }



    public TaskResponseDTO mapToResponseDTO(Task task) {
        TaskResponseDTO dto = new TaskResponseDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setDeadline(task.getDeadline());
        dto.setStatus(task.getStatus().name());
        dto.setPriority(
                task.getPriority() != null ? task.getPriority().name() : null
        );

        dto.setUserId(task.getUser().getId());
        // 🔁 recurrence
        dto.setRecurring(task.isRecurring());
        dto.setRecurrenceActive(task.isRecurrenceActive());
        dto.setRecurrenceStopped(task.isRecurrenceStopped());
        dto.setRepeatInterval(
                task.getRepeatInterval() != null ? task.getRepeatInterval().name() : null
        );
        dto.setRecurrenceEndDate(task.getRecurrenceEndDate());

        return dto;
    }




    private Priority calculatePriority(Task task) {
        if (!task.isAutoPriorityEnabled()) {
            return task.getPriority(); // manual priority, don’t overwrite
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = task.getDeadline();

        if (deadline == null) return Priority.LOW;

        if (deadline.isBefore(now)) return Priority.CRITICAL;

        long hoursLeft = java.time.Duration.between(now, deadline).toHours();

        if (hoursLeft < 24) return Priority.HIGH;
        else if (hoursLeft < 72) return Priority.MEDIUM;
        else return Priority.LOW;
    }


    public void snooze(Long taskId, int days) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (task.isRecurring()) {
            throw new IllegalStateException("Cannot snooze a recurring task");
        }
        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Completed task cannot be modified"
            );
        }


        task.setDeadline(task.getDeadline().plusDays(days));
        task.setStatus(TaskStatus.PENDING);
        resetReminders(task);
        taskRepository.save(task);
    }

    public void postpone(Long taskId, int days) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        if (task.isRecurring()) {
            throw new IllegalStateException("Cannot postpone a recurring task");
        }

        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Completed task cannot be modified"
            );
        }

        task.setDeadline(task.getDeadline().plusDays(days));
        task.setStatus(TaskStatus.PENDING);
        resetReminders(task);
        taskRepository.save(task);
    }

    private void resetReminders(Task task) {
        task.setReminded5Days(false);
        task.setReminded3Days(false);
        task.setReminded1Day(false);
        task.setReminded10Hours(false);
        task.setReminded5Hours(false);
        task.setReminded1Hour(false);
        task.setOverdueAlertSent(false);
    }


    @Transactional
    public void deleteSingleTask(Long taskId) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        validateOwner(task);
        if (isLatestRecurring(task)) {
            throw new InvalidTaskOperationException(
                    "You cannot delete this task because it is required to generate the next recurring task."
            );
        }





        taskRepository.delete(task);
    }
    private boolean isLatestRecurring(Task task) {
        if (!task.isRecurring()) return false;
        Task latest = taskRepository.findTopByUserIdAndRecurringOrderByDeadlineDesc(task.getUser().getId(), true);
        return latest.getId().equals(task.getId());
    }


    private void validateOwner(Task task) {
        String email = securityUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!task.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Not allowed");
        }
    }
    public Task setTaskPriority(Long taskId, Priority priority) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        task.setPriority(priority);
        task.setAutoPriorityEnabled(false);

       return taskRepository.save(task);
    }
    private void validateStatusTransition(TaskStatus current, TaskStatus next) {

        if (current == TaskStatus.COMPLETED) {
            throw new IllegalStateException("Completed task cannot change status");
        }

        if (current == TaskStatus.OVERDUE && next != TaskStatus.COMPLETED) {
            throw new IllegalStateException("Overdue task can only be completed");
        }

        if (current == TaskStatus.PENDING && next == TaskStatus.COMPLETED) {
            throw new IllegalStateException("Move task to IN_PROGRESS before completing");
        }
    }
    @Transactional
    public void pauseRecurrence(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        validateOwner(task);
        if (!task.isRecurring()) {
            throw new IllegalStateException("Task is not recurring");
        }
        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Completed task cannot be modified"
            );
        }


        task.setRecurrenceActive(false);
    }

    @Transactional
    public void resumeRecurrence(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        validateOwner(task);
        if (!task.isRecurring()) {
            throw new IllegalStateException("Task is not recurring");
        }
        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Completed task cannot be modified"
            );
        }


        if (task.isRecurrenceStopped())
            throw new IllegalStateException("Recurrence already stopped");
        task.setRecurrenceActive(true);
        // 🔁 Generate missed task ONLY if needed
        if (task.getStatus() == TaskStatus.COMPLETED
                && !task.isNextRecurringGenerated()) {

            generateNextRecurringTask(task);
        }
    }

    @Transactional
    public void stopRecurrence(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        validateOwner(task);
        if (!task.isRecurring()) {
            throw new IllegalStateException("Task is not recurring");
        }
        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Completed task cannot be modified"
            );
        }


        task.setRecurrenceStopped(true);
    }
    public Page<TaskResponseDTO> getDeletedTasks(int page, int size) {
        User user = getLoggedInUser();
        Pageable pageable = PageRequest.of(page, size);

        return taskRepository
                .findByUserIdAndDeletedTrue(user.getId(), pageable)
                .map(this::mapToResponseDTO);
    }
    @Transactional
    public void restore(Long taskId) {
        Task task = getOwnedTask(taskId);
        task.setDeleted(false);
        task.setDeletedAt(null);
    }
    @Transactional
    public void softDelete(Long taskId) {
        Task task = getOwnedTask(taskId);
        task.setDeleted(true);
        task.setDeletedAt(LocalDateTime.now());
        taskRepository.save(task);
    }
    private User getLoggedInUser() {
        String email = securityUtil.getCurrentUserEmail();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
    private Task getOwnedTask(Long taskId) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        User user = getLoggedInUser();

        if (!task.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not allowed to access this task");
        }

        return task;
    }











}