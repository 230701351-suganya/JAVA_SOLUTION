// 1️⃣ Package declaration
// Idhu indha class endha folder (namespace) la irukku nu sollum
// Java-la same name class clash avoid panna use aagum
package com.todo.smarttodo.controller;


// 2️⃣ Import statements
// Vera packages / classes indha file la use panna import venum

import com.todo.smarttodo.entity.Priority;
import com.todo.smarttodo.entity.Task;
// ❗️(Actually ippo use pannala, DTO use panrom – later remove pannalam)

import com.todo.smarttodo.entity.TaskStatus;
// TaskStatus enum (PENDING, COMPLETED etc)

import com.todo.smarttodo.service.TaskService;
// Business logic irukkura service class

import jakarta.validation.Valid;
// @Valid -> request body validation trigger panna

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
// Date format conversion (commented code la use pannirundha)

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
// RestController, GetMapping, PostMapping etc ellathukum


import com.todo.smarttodo.dto.TaskRequestDTO;
// Client -> backend varra data

import com.todo.smarttodo.dto.TaskResponseDTO;
// Backend -> client pogra data

import java.awt.print.Pageable;
import java.time.LocalDateTime;
// Date & time handle panna

import java.util.List;
// Multiple tasks return panna

// 3️⃣ @RestController
// Idhu Spring-ku sollum:
// "Indha class REST API handle pannum"
// + @Controller + @ResponseBody combo

@RestController
// 4️⃣ Base URL mapping
// Indha controller ulla ellā API-kkum base path
// example: /tasks/create, /tasks/user/1
@RequestMapping("/tasks")
public class TaskController {
    // 5️⃣ Service object
    // Controller direct-a DB-oda pesaadhu
    // Always service moolama dhaan pogum
    private final TaskService taskService;

    // Constructor Injection
    // Spring automatically TaskService object create panni
    // indha constructor-la inject pannum

    // WHY constructor?
    // ✅ Recommended
    // ✅ Immutable
    // ✅ Easy testing
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // Create task for a user
    //“@DateTimeFormat tells Spring the exact date format so it can convert request text into a Java date-time object.”
    //@PostMapping("/create")
    //public Task createTask(@RequestParam Long userId,
    //                       @RequestParam String title,
    //                       @RequestParam(required = false) String description,@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime deadline) {
    //                       return taskService.createTask(userId, title, description,deadline);
    //                       }
    // 7️⃣ POST request
    // Client new task create panna use pannum
    // URL: POST /tasks/create


    @PostMapping("/create")
    public TaskResponseDTO createTask(
                                      @Valid @RequestBody TaskRequestDTO taskRequestDTO) {
        return taskService.createTask(taskRequestDTO);
    }


    //@GetMapping("/user/{userId}")
    //public List<TaskResponseDTO> getTasksByUser(@PathVariable Long userId) {
        //return taskService.getTasksByUser(userId);
    //}
    @GetMapping
    public Page<TaskResponseDTO> getTasksByUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return taskService.getTasksByUser(page, size);
    }




    // Update task status
    // @PutMapping("/{taskId}/status")
    // public Task updateTaskStatus(@PathVariable Long taskId,
                // @RequestParam TaskStatus status) {
    // return taskService.updateTaskStatus(taskId, status);
    //}
    @PutMapping("/{taskId}/status")
    public TaskResponseDTO updateTaskStatus(@PathVariable Long taskId,
                                            @RequestParam TaskStatus status) {
        return taskService.updateTaskStatus(taskId, status);
    }


    //Delete task
    @DeleteMapping("/{taskId}")
    public void deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
    }

    @PutMapping("/{taskId}")
    public TaskResponseDTO updateTask(@PathVariable Long taskId,
                                      @RequestBody TaskRequestDTO taskRequestDTO) {
        return taskService.updateTask(taskId, taskRequestDTO);
    }
    @PutMapping("/{taskId}/priority")
    public TaskResponseDTO setTaskPriority(
            @PathVariable Long taskId,
            @RequestParam Priority priority) {

        Task task = taskService.setTaskPriority(taskId, priority);
        return taskService.mapToResponseDTO(task);
    }
    @DeleteMapping("/deleteSingle/{id}")
    public ResponseEntity<Void> deleteSingleTask(@PathVariable Long id) {
        taskService.deleteSingleTask(id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{taskId}/soft-delete")
    public ResponseEntity<Void> softDelete(@PathVariable Long taskId) {
        taskService.softDelete(taskId);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{taskId}/restore")
    public ResponseEntity<Void> restore(@PathVariable Long taskId) {
        taskService.restore(taskId);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/trash")
    public Page<TaskResponseDTO> getTrash(
            @RequestParam int page,
            @RequestParam int size) {
        return taskService.getDeletedTasks(page, size);
    }
    @DeleteMapping("/trash/empty")
    public ResponseEntity<Void> emptyTrash() {
        taskService.emptyTrash();
        return ResponseEntity.noContent().build();
    }



    // Pause recurrence
    @PostMapping("/{taskId}/pause")
    public ResponseEntity<String> pauseRecurrence(@PathVariable Long taskId) {
        taskService.pauseRecurrence(taskId);
        return ResponseEntity.ok("Recurrence paused successfully");
    }

    // Resume recurrence
    @PostMapping("/{taskId}/resume")
    public ResponseEntity<String> resumeRecurrence(@PathVariable Long taskId) {
        taskService.resumeRecurrence(taskId);
        return ResponseEntity.ok("Recurrence resumed successfully");
    }

    // Stop recurrence
    @PostMapping("/{taskId}/stop")
    public ResponseEntity<String> stopRecurrence(@PathVariable Long taskId) {
        taskService.stopRecurrence(taskId);
        return ResponseEntity.ok("Recurrence stopped successfully");
    }





}
